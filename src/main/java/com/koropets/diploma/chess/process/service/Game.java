package com.koropets.diploma.chess.process.service;

import com.koropets.diploma.chess.process.constants.Constants;
import com.koropets.diploma.chess.utils.ProcessingUtils;
import com.koropets.diploma.chess.model.Field;
import com.koropets.diploma.chess.model.Figure;
import com.koropets.diploma.chess.model.Color;
import com.koropets.diploma.chess.model.Board;
import com.koropets.diploma.chess.model.King;
import com.koropets.diploma.chess.model.Rock;
import com.koropets.diploma.chess.model.Bishop;
import com.koropets.diploma.chess.model.Queen;
import com.koropets.diploma.chess.model.Pawn;
import com.koropets.diploma.chess.model.Observer;
import com.koropets.diploma.chess.process.dto.Turn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class Game {

    private Set<Turn> possibleTurnsAndEating = new LinkedHashSet<Turn>();
    private int numberOfTurn;

    public final Field f1 = new Field(7, 5);
    public final Field g1 = new Field(7, 6);
    public final Field b1 = new Field(7, 1);
    public final Field c1 = new Field(7, 2);
    public final Field d1 = new Field(7, 3);
    public final Field f8 = new Field(0, 5);
    public final Field g8 = new Field(0, 6);
    public final Field b8 = new Field(0, 1);
    public final Field c8 = new Field(0, 2);
    public final Field d8 = new Field(0, 3);
    public final Field a1 = new Field(7, 0);
    public final Field h1 = new Field(7, 7);
    public final Field e1 = new Field(7, 4);
    public final Field a8 = new Field(0, 0);
    public final Field h8 = new Field(0, 7);
    public final Field e8 = new Field(0, 4);

    @Autowired
    private Board board;

    public Set<Turn> getPossibleTurnsAndEatings(Color color, int numberOfTurn) {
        this.numberOfTurn = numberOfTurn;
        setPossibleTurnsAndEating(color);
        possibleTurnsAndEating = possibleTurnsAndEating.stream().filter(turn -> turn != null).collect(Collectors.toSet());
        return possibleTurnsAndEating;
    }

    private void setPossibleTurnsAndEating(Color color){
        possibleTurnsAndEating.clear();
        King king = board.getKing(color);
        List<Observer> allies = board.getFigures(color).stream().filter(a -> a.getClass() != King.class).collect(Collectors.toList());

        if (king.isUnderAttack() && king.getEnemiesAttackMe().size() == 1){
            List<Tuple2<Figure, Field>> kingTuple2 = new ArrayList<>();
            for (Figure enemy : king.getWhoCouldBeEaten()){
                if (enemy.getAlliesProtectMe().size() == 0){
                    kingTuple2.add(new Tuple2<>(king, enemy.getField()));
                    possibleTurnsAndEating.add(ProcessingUtils.createTurn(kingTuple2, null, "", true, false, false, enemy, numberOfTurn));
                }
            }
            Figure whoAttackKing = king.getEnemiesAttackMe().iterator().next();
            for (Observer observer : allies){
                Figure ally = (Figure) observer;
                if (whoAttackKing.getClass() == Pawn.class && ally.getClass() == Pawn.class){
                    Pawn pawnAlly = (Pawn) ally;
                    if (enPassantCanSaveKing(pawnAlly, whoAttackKing)){
                        List<Tuple2<Figure, Field>> alienToTargetField = new ArrayList<>();
                        alienToTargetField.add(new Tuple2<>(pawnAlly, pawnAlly.getEnPassantField()));
                        possibleTurnsAndEating.add(ProcessingUtils.createTurn(alienToTargetField, null, "", true, false, true, whoAttackKing, numberOfTurn));
                    }
                }else if ((color == Color.WHITE && whoAttackKing.getField().getX() == Constants.LINE_H) || (color == Color.BLACK && whoAttackKing.getField().getX() == Constants.LINE_A)){
                    Pawn pawnAlly = (Pawn) ally;
                    if (pawnReachesLastLineCanSaveKing(pawnAlly, whoAttackKing)){
                        possibleTurnsAndEating.addAll(setTransformationFields(pawnAlly, whoAttackKing, color, true));

                    }
                }else if (ally.getWhoCouldBeEaten().contains(whoAttackKing)){
                    List<Tuple2<Figure, Field>> alienToTargetField = new ArrayList<>();
                    alienToTargetField.add(new Tuple2<>(ally, whoAttackKing.getField()));
                    possibleTurnsAndEating.add(ProcessingUtils.createTurn(alienToTargetField, null, "", true, false, false, whoAttackKing, numberOfTurn));
                }
            }
            peacefulTurn(king);

            Figure figureAttacksKing = king.getEnemiesAttackMe().iterator().next();
            Set<Turn> alienCovers = new HashSet<>();
            if (figureAttacksKing instanceof Rock){
                alienCovers = coveringIfRockAttacks(king, (Rock) figureAttacksKing);
            }
            if (figureAttacksKing instanceof Bishop){
                alienCovers = coveringIfBishopAttacks(king, (Bishop) figureAttacksKing);
            }
            if (figureAttacksKing instanceof Queen){
                alienCovers = coveringIfQueenAttacks(king, (Queen) figureAttacksKing);
            }

            if (alienCovers != null){
                possibleTurnsAndEating.addAll(alienCovers);
            }

        }
        if (king.isUnderAttack() && king.getEnemiesAttackMe().size() > 1){
            peacefulTurn(king);
        }

        if (!king.isUnderAttack()){
            for (Observer observer : allies){
                Figure ally = (Figure) observer;
                if (ally.getClass() == Pawn.class && ((Pawn)ally).isEnPassant()){
                    possibleTurnsAndEating.addAll(turnsInCaseEnPassant((Pawn) ally));

                }else if(ally.getClass() == Pawn.class && ((Pawn)ally).isOnThePenultimateLine()){
                    possibleTurnsAndEating.addAll(turnsInCaseTransformation(ally, color));

                }else{
                    peacefulTurn(ally);
                    for (Figure attackedFigure : ally.getWhoCouldBeEaten()){
                        List<Tuple2<Figure, Field>> figureFieldTuple = new ArrayList<>();
                        figureFieldTuple.add(new Tuple2<Figure, Field>(ally, attackedFigure.getField()));
                        possibleTurnsAndEating.add(ProcessingUtils.createTurn(figureFieldTuple, null, "", true, false, false,  attackedFigure, numberOfTurn));
                    }
                }
            }
            possibleTurnsAndEating.addAll(castling(color));
        }
    }

    private void peacefulTurn(Figure figure){
        for (Field field : figure.getPossibleFieldsToMove()){
            List<Tuple2<Figure, Field>> figureToFieldTuple = new ArrayList<>();
            figureToFieldTuple.add(new Tuple2<>(figure, field));
            possibleTurnsAndEating.add(ProcessingUtils.createTurn(figureToFieldTuple, null, "", false, false, false, null, numberOfTurn));
        }
    }

    private Set<Turn> coveringIfRockAttacks(final King king, final Rock enemyRock){
        Set<Turn> coveringTurns = new HashSet<>();
        Set<Field> fieldsBetween = ProcessingUtils.fieldsBetweenRockAndKing(king, enemyRock.getField());
        List<Observer> alienFigures = board.getFigures(king.getColor());
        setCoveringTurns(alienFigures, coveringTurns, fieldsBetween);
        return coveringTurns;
    }

    private Set<Turn> coveringIfBishopAttacks(final King king, final Bishop bishop){
        Set<Field> fieldsBetween = ProcessingUtils.fieldsBetweenBishopAndKing(king, bishop.getField());
        Set<Turn> coveringTurns = new HashSet<>();
        List<Observer> alienFigures = board.getFigures(king.getColor());
        setCoveringTurns(alienFigures, coveringTurns, fieldsBetween);
        return coveringTurns;
    }

    private Set<Turn> coveringIfQueenAttacks(final King king, final Queen queen){
        Set<Field> fieldsBetween = ProcessingUtils.fieldsBetweenQueenAndKing(king, queen.getField());
        Set<Turn> coveringTurns = new HashSet<>();
        List<Observer> alienFigures = board.getFigures(king.getColor());
        setCoveringTurns(alienFigures, coveringTurns, fieldsBetween);
        return coveringTurns;
    }

    private void setCoveringTurns(final List<Observer> alienFigures, final Set<Turn> coveringTurns, final Set<Field> fieldsBetween){
        alienFigures.stream().filter(v -> v.getClass() != King.class).forEach(f ->{
            ((Figure)f).getPossibleFieldsToMove().forEach(k -> {
                if (fieldsBetween.contains(k)){
                    if (f.getClass() == Pawn.class && ((Pawn)f).isOnThePenultimateLine()){
                        for (String writtenStypeTurn : ProcessingUtils.FIGURES_IN_WRITTEN_STYLE){
                            List<Tuple2<Figure, Field>> covering = new ArrayList<>();
                            covering.add(new Tuple2<>((Figure)f, k));
                            coveringTurns.add(ProcessingUtils.createTurn(covering, ProcessingUtils.createFigure(k, writtenStypeTurn, ((Figure)f).getColor()),
                                    "", false, true, false, null, numberOfTurn));
                        }
                    }
                    List<Tuple2<Figure, Field>> covering = new ArrayList<>();
                    covering.add(new Tuple2<>((Figure)f, k));
                    coveringTurns.add(ProcessingUtils.createTurn(covering, null, "", false, false, false, null, numberOfTurn));
                }
            });
        });
    }

    private List<Turn> castling(Color color){
        List<Turn> castlings = new ArrayList<>();
        List<Figure> rocks = board.getFiguresByClass(Rock.class, color);
        King king = (King) board.getFiguresByClass(King.class, color).get(0);
        for (Figure rock : rocks){
            if ((color == Color.BLACK && rock.getField().equals(h8)) || (color == Color.WHITE && rock.getField().equals(h1))){
                castlings.add(shortCastling((Rock)rock, king, color));
            }
            if ((color == Color.BLACK && rock.getField().equals(a8)) || (color == Color.WHITE && rock.getField().equals(a1))){
                castlings.add(longCastling((Rock) rock, king, color));
            }
        }
        return castlings;
    }

    private Turn shortCastling(Rock rock, King king, Color color){
        Turn shortCastlingTurn = null;
        List<Tuple2<Figure, Field>> castlingTuple = new ArrayList<>();
        if (rock.isOpportunityToCastling() && king.isOpportunityToCastling()){
            if (color == Color.BLACK){
                if (!board.getFieldsUnderWhiteInfluence().contains(f8) && !board.getFieldsUnderWhiteInfluence().contains(g8) &&
                        board.getFieldToFigure().get(f8) == null && board.getFieldToFigure().get(g8) == null){
                    castlingTuple.add(new Tuple2<>(king, g8));
                    castlingTuple.add(new Tuple2<>(rock, f8));
                    shortCastlingTurn = ProcessingUtils.createTurn(castlingTuple, null, Constants.shortCastling, false, false, false, null, numberOfTurn);
                }
            }else{
                if (!board.getFieldsUnderBlackInfluence().contains(f1) && !board.getFieldsUnderBlackInfluence().contains(g1) &&
                        board.getFieldToFigure().get(f1) == null && board.getFieldToFigure().get(g1) == null){
                    castlingTuple.add(new Tuple2<>(king, g1));
                    castlingTuple.add(new Tuple2<>(rock, f1));
                    shortCastlingTurn = ProcessingUtils.createTurn(castlingTuple, null, Constants.shortCastling, false, false, false, null, numberOfTurn);
                }
            }
        }
        return shortCastlingTurn;
    }

    private Turn longCastling(Rock rock, King king, Color color){
        Turn longCastlingTurn = null;
        List<Tuple2<Figure, Field>> castlingTuple = new ArrayList<>();
        if (rock.isOpportunityToCastling() && king.isOpportunityToCastling()){
            if (color == Color.BLACK){
                if (!board.getFieldsUnderWhiteInfluence().contains(b8) && !board.getFieldsUnderWhiteInfluence().contains(c8) &&
                        !board.getFieldsUnderWhiteInfluence().contains(d8) && board.getFieldToFigure().get(b8) == null &&
                        board.getFieldToFigure().get(c8) == null && board.getFieldToFigure().get(d8) == null){
                    castlingTuple.add(new Tuple2<>(king, c8));
                    castlingTuple.add(new Tuple2<>(rock, d8));
                    longCastlingTurn = ProcessingUtils.createTurn(castlingTuple, null, Constants.longCastling, false, false, false,  null, numberOfTurn);
                }
            }else {
                if (!board.getFieldsUnderBlackInfluence().contains(b1) && !board.getFieldsUnderBlackInfluence().contains(c1) &&
                        !board.getFieldsUnderBlackInfluence().contains(d1) && board.getFieldToFigure().get(b1) == null &&
                        board.getFieldToFigure().get(c1) == null && board.getFieldToFigure().get(d1) == null){
                    castlingTuple.add(new Tuple2<>(king, c1));
                    castlingTuple.add(new Tuple2<>(rock, d1));
                    longCastlingTurn = ProcessingUtils.createTurn(castlingTuple, null, Constants.longCastling, false, false, false, null, numberOfTurn);
                }
            }
        }
        return longCastlingTurn;
    }

    private boolean enPassantCanSaveKing(Pawn pawnAlly, Figure pawnEnemy){
        return pawnAlly.isEnPassant() && pawnAlly.getEnPassantEnemy().equals(pawnEnemy);
    }

    private boolean pawnReachesLastLineCanSaveKing(Pawn pawnAlly, Figure enemy){
        return pawnAlly.isOnThePenultimateLine() && pawnAlly.getWhoCouldBeEaten().contains(enemy);
    }

    private Set<Turn> turnsInCaseEnPassant(Pawn ally){
        Set<Turn> possibleTurns = new HashSet<>();
        Figure enPassantEnemy = ally.getEnPassantEnemy();
        for (Field field : ally.getPossibleFieldsToMove()){
            List<Tuple2<Figure, Field>> figureToFieldTupleList = new ArrayList<>();
            figureToFieldTupleList.add(new Tuple2<>(ally, field));
            possibleTurns.add(ProcessingUtils.createTurn(figureToFieldTupleList, null, "",
                    false, false, false, null, numberOfTurn));

        }
        for (Figure enemy : ally.getWhoCouldBeEaten()){
            if (!enemy.equals(enPassantEnemy)){
                List<Tuple2<Figure, Field>> figureToFieldTupleList = new ArrayList<>();
                figureToFieldTupleList.add(new Tuple2<>(ally, enemy.getField()));
                possibleTurns.add(ProcessingUtils.createTurn(figureToFieldTupleList, null, "",
                        true, false, false,  enemy, numberOfTurn));
            }
        }
        if (enPassantEnemy != null){
            List<Tuple2<Figure, Field>> figureToFieldList = new ArrayList<>();
            figureToFieldList.add(new Tuple2<>(ally, ally.getEnPassantField()));
            possibleTurns.add(ProcessingUtils.createTurn(figureToFieldList, null, "", true,
                    false, true,  enPassantEnemy, numberOfTurn));
        }
        return possibleTurns;
    }

    private Set<Turn> turnsInCaseTransformation(Figure ally, Color color){
        Set<Turn> possibleTurns = new HashSet<>();
        for (Field possibleFieldToMove : ally.getPreyField()){
            for (String writtenStyle: ProcessingUtils.FIGURES_IN_WRITTEN_STYLE){
                List<Tuple2<Figure, Field>> figureToFieldTupleList = new ArrayList<>();
                figureToFieldTupleList.add(new Tuple2<>(ally, possibleFieldToMove));
                possibleTurns.add(ProcessingUtils.createTurn(figureToFieldTupleList, ProcessingUtils.createFigure(possibleFieldToMove, writtenStyle, color),
                        "", false, true, false, null, numberOfTurn));

            }
        }
        for (Figure enemy : ally.getWhoCouldBeEaten()){
            for (String writtenStyle : ProcessingUtils.FIGURES_IN_WRITTEN_STYLE){
                List<Tuple2<Figure, Field>> figureToFieldTupleList = new ArrayList<>();
                figureToFieldTupleList.add(new Tuple2<>(ally, enemy.getField()));
                possibleTurns.add(ProcessingUtils.createTurn(figureToFieldTupleList, ProcessingUtils.createFigure(enemy.getField(), writtenStyle, color),
                        "", true, true, false, enemy, numberOfTurn));

            }
        }
        return possibleTurns;
    }

    private Set<Turn> setTransformationFields(Pawn pawn, Figure enemy, Color color, boolean eating){
        Set<Turn> transformationSet = new HashSet<>();
        for (String writtenStyleOfFigure : ProcessingUtils.FIGURES_IN_WRITTEN_STYLE){
            List<Tuple2<Figure, Field>> allyToFieldList = new ArrayList<>();
            allyToFieldList.add(new Tuple2<Figure, Field>(pawn, enemy.getField()));
            Turn newTransformationTurn = ProcessingUtils.createTurn(allyToFieldList, ProcessingUtils.createFigure(enemy.getField(), writtenStyleOfFigure, color),
                    "", eating, true, false,  enemy, numberOfTurn);
            transformationSet.add(newTransformationTurn);
        }
        return transformationSet;
    }
}