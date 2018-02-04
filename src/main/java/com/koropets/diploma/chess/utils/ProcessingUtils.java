package com.koropets.diploma.chess.utils;

import com.koropets.diploma.chess.model.Board;
import com.koropets.diploma.chess.model.Color;
import com.koropets.diploma.chess.model.King;
import com.koropets.diploma.chess.model.Pawn;
import com.koropets.diploma.chess.model.Bishop;
import com.koropets.diploma.chess.model.Observer;
import com.koropets.diploma.chess.model.Field;
import com.koropets.diploma.chess.model.Figure;
import com.koropets.diploma.chess.model.Rock;
import com.koropets.diploma.chess.model.Knight;
import com.koropets.diploma.chess.model.Queen;
import com.koropets.diploma.chess.process.dto.Turn;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import scala.Tuple2;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import static java.lang.Math.abs;
import static com.koropets.diploma.chess.process.constants.Constants.longCastling;
import static com.koropets.diploma.chess.process.constants.Constants.shortCastling;
import static com.koropets.diploma.chess.process.constants.Constants.SIZE;

/**
 * @author AndriiKoropets
 */
@UtilityClass
@Slf4j
public class ProcessingUtils {


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

    private List<Observer> candidates;
    private String mainTurn;
    private Field field;
    private int number;
    private Set<Field> affectedFields;
    private List<Tuple2<Figure, Field>> figureToField = new ArrayList<>();
    private Figure figure;
    private boolean isEating;
    private boolean transformation;
    private Figure targetedFigure;
    private final Field whiteKingShortCastling = new Field(7, 6);
    private final Field whiteKingLongCastling = new Field(7, 2);
    private final Field blackKingShortCastling = new Field(0, 6);
    private final Field blackKingLongCastling = new Field(0, 2);
    private final Field whiteRockShortCastling = new Field(7, 5);
    private final Field whiteRockLongCastling = new Field(7, 3);
    private final Field blackRockShortCastling = new Field(0, 5);
    private final Field blackRockLongCastling = new Field(0, 3);
    private final FrequentFigure whiteFrequent = new FrequentFigure();
    private final FrequentFigure blackFrequent = new FrequentFigure();
    private final String PLUS = "+";
    public final Set<String> FIGURES_IN_WRITTEN_STYLE = new HashSet<>(Arrays.asList("R", "N", "B", "Q"));
    private List<Tuple2<Figure, Field>> tuplesFigureToField;
    private Figure figureBornFromTransformation;
    private String figureInWrittenStyleToBorn;
    public Figure eatenFigureToResurrection;

    @Autowired
    private Board board;

    public Turn getActualTurn(final String turnWrittenStyle, final Color color, int numberOfTurn){
        candidates = new ArrayList<>();
        mainTurn = turnWrittenStyle;
        number = numberOfTurn;
        field = parseTargetField(turnWrittenStyle);
        return setTurn(turnWrittenStyle, color);
    }

    public Turn getPossibleTurn(){
        //TODO implement logic for possible turns which was not made in party
        return null;
    }

    public Tuple2<FrequentFigure, FrequentFigure> countFrequent(boolean isWhite, String writtenTurn){
        FrequentFigure frequent = isWhite ? whiteFrequent : blackFrequent;
        char figure = writtenTurn.charAt(0);
        switch (figure){
            case 'R' :  frequent.updateRock();
            case 'N' : frequent.updateKnight();
            case 'B' : frequent.updateBishop();
            case 'Q' : frequent.updateQueen();
            case 'K' : frequent.updateKing();
            case '0' : frequent.updateKing();
            default: frequent.updatePawn();
        }
        return new Tuple2<>(whiteFrequent, blackFrequent);
    }

    private void initialize(){
        candidates.clear();
        figureToField.clear();
        figure = null;
        targetedFigure = null;
        isEating = false;
        figureBornFromTransformation = null;
        figureInWrittenStyleToBorn = "";
        transformation = whetherWrittenTurnIsTransformation();
    }

    private Turn setTurn(final String writtenStyle, final Color color){
        initialize();
        if (shortCastling.equals(writtenStyle)){
            List<Tuple2<Figure, Field>> figureToField = new ArrayList<>();
            if (color == Color.WHITE){
                Figure whiteKing = board.getFieldToFigure().get(e1);
                Figure whiteRock_H = board.getFieldToFigure().get(h1);
                figureToField.add(new Tuple2<>(whiteKing, whiteKingShortCastling));
                figureToField.add(new Tuple2<>(whiteRock_H, whiteRockShortCastling));
            }else {
                Figure blackKing = board.getFieldToFigure().get(e8);
                Figure blackRock_H = board.getFieldToFigure().get(h8);
                figureToField.add(new Tuple2<>(blackKing, blackKingShortCastling));
                figureToField.add(new Tuple2<>(blackRock_H, blackRockShortCastling));
            }
            return createTurn(figureToField, null, writtenStyle, false, false, false, null, number);
        }
        if (longCastling.equals(writtenStyle)){
            List<Tuple2<Figure, Field>> figureToField = new ArrayList<>();
            if (color == Color.WHITE){
                Figure whiteKing = board.getFieldToFigure().get(e1);
                Figure whiteRock_A = board.getFieldToFigure().get(a1);
                figureToField.add(new Tuple2<>(whiteKing, whiteKingLongCastling));
                figureToField.add(new Tuple2<>(whiteRock_A, whiteRockLongCastling));
            }else {
                Figure blackKing = board.getFieldToFigure().get(e8);
                Figure blackRock_A = board.getFieldToFigure().get(a8);
                figureToField.add(new Tuple2<>(blackKing, blackKingLongCastling));
                figureToField.add(new Tuple2<>(blackRock_A, blackRockLongCastling));
            }
            return createTurn(figureToField, null, writtenStyle, false, false, false, null, number);
        }
        char firstCharacter = writtenStyle.charAt(0);
        switch (firstCharacter){
            case 'R' :  figureToField = writtenStyle.contains("x") ? fetchFigureToTargetField(Rock.class, color, true, false) : fetchFigureToTargetField(Rock.class, color, false, false);
                        return createTurn(figureToField, null, writtenStyle, isEating, false, false, targetedFigure, number);
            case 'N' :  figureToField = writtenStyle.contains("x") ? fetchFigureToTargetField(Knight.class, color, true, false) : fetchFigureToTargetField(Knight.class, color, false, false);
                        return createTurn(figureToField, null, writtenStyle, isEating, false, false, targetedFigure, number);
            case 'B' :  figureToField = writtenStyle.contains("x") ? fetchFigureToTargetField(Bishop.class, color, true, false) : fetchFigureToTargetField(Bishop.class, color, false, false);
                        return createTurn(figureToField, null, writtenStyle, isEating, false, false, targetedFigure, number);
            case 'Q' :  figureToField = writtenStyle.contains("x") ? fetchFigureToTargetField(Queen.class, color, true, false) : fetchFigureToTargetField(Queen.class, color, false, false);
                        return createTurn(figureToField, null, writtenStyle, isEating, false, false, targetedFigure, number);
            case 'K' :  figureToField = writtenStyle.contains("x") ? fetchFigureToTargetField(King.class, color, true, false) : fetchFigureToTargetField(King.class, color, false, false);
                        return createTurn(figureToField, null, writtenStyle, isEating, false, false, targetedFigure, number);
            default :   figureToField = writtenStyle.contains("x") ? fetchFigureToTargetField(Pawn.class, color, true, transformation) : fetchFigureToTargetField(Pawn.class, color, false, transformation);
                        return isEnPassantScenario(figureToField) ? createTurn(figureToField, null, writtenStyle, isEating, transformation, true, targetedFigure, number) :
                                createTurn(figureToField, null, writtenStyle, isEating, transformation, false, targetedFigure, number);
        }
    }

    private List<Tuple2<Figure, Field>> fetchFigureToTargetField(Class clazz, Color color, boolean eating, boolean transformation){
        isEating = eating;
        List<Observer> targets = new ArrayList<Observer>();
        List<Figure> figures = board.getFiguresByClass(clazz, color);
        for (Observer curFigure : figures){
            if (eating){
                if (transformation && clazz == Pawn.class){
                    Pawn pawn = (Pawn) curFigure;
                    if (pawn.getPreyField().contains(field)){
                        targets.add(curFigure);
                        targetedFigure = board.getFieldToFigure().get(field);
                        figureBornFromTransformation = createFigure(field, figureInWrittenStyleToBorn, pawn.getColor());
                    }
                }
                if (clazz == Pawn.class && ((Pawn) curFigure).isEnPassant()){
                    Pawn pawn = (Pawn) curFigure;
                    if (pawn.getEnPassantField().equals(field)){
                        targets.add(pawn);
                        targetedFigure = pawn.getEnPassantEnemy();
                        System.out.println("Here... passed " + targetedFigure + " " + targets);
                        System.out.println("who could be eaten = " + pawn.getWhoCouldBeEaten() + " aleis I protect = "
                                + pawn.getAlliesIProtect() + " enemy fields = " + pawn.getPreyField() + " enPassant enemy = "
                        + pawn.getEnPassantEnemy());
                    }
                }else {
                    System.out.println("Class = "  + clazz + " color = " + color + " eating = " + eating);
                    if (((Figure) curFigure).getPreyField().contains(field)){
                        targets.add(curFigure);
                        targetedFigure = board.getFieldToFigure().get(field);
//                        System.out.println("targeted figure = " + targetedFigure);
                    }
                }
            }else {
                if (transformation && clazz == Pawn.class){
                    Pawn pawn = (Pawn) curFigure;
                    if (pawn.getPossibleFieldsToMove().contains(field)){
                        candidates.add(pawn);
                        figureBornFromTransformation = createFigure(field, figureInWrittenStyleToBorn, color);
                    }
                }
                if (((Figure) curFigure).getPossibleFieldsToMove().contains(field)){
                    candidates.add(curFigure);
                }
            }
        }
        if (!targets.isEmpty()){
            System.out.println("targets = " + targets);
            if (targets.size() == 1){
                figure = (Figure)targets.get(0);
                System.out.println("Figure = " + figure);
            }else{
                figure = choseFigureWhichAttack(targets, clazz);
            }
        }
        if (!candidates.isEmpty()){
            if (candidates.size() > 1){
                figure = choseExactFigure(candidates);
            }else {
                figure = (Figure) candidates.get(0);
            }
        }
        if (figure != null){
            figureToField.add(new Tuple2<>(figure, field));
        }
        if (figureToField.size() == 0){
            throw new RuntimeException("Could not fetch figure. Turn must be wrong written. Turn = " + mainTurn);
        }
        return figureToField;
    }

    private Figure choseFigureWhichAttack(List<Observer> targets, Class clazz){
        if (clazz == Pawn.class){
            char verticalPawn = mainTurn.charAt(0);
            for (Object currentFigure : targets){
                if (((Figure) currentFigure).getField().getY() == Field.getInvertedHorizontal().get(verticalPawn)){
                    return (Figure) currentFigure;
                }
            }
        }else {
            char secondPosition = mainTurn.charAt(1);
            System.out.println("SecondPosition = " + secondPosition);
            int integer = Character.getNumericValue(secondPosition);
            System.out.println("integer = " + integer);
            return chose(integer, secondPosition, targets);
        }
        return null;
    }

    private Figure choseExactFigure(List<Observer> targets){
        char secondPosition = mainTurn.charAt(1);
        int integer = Character.getNumericValue(secondPosition);
        return chose(integer, secondPosition, targets);
    }

    private Figure chose(int integer, char secondPosition, List<Observer> candidatesForBeingTheOne){
        System.out.println("candidates = " + candidatesForBeingTheOne);
        for (Observer observer : candidatesForBeingTheOne){
            if (integer > SIZE){
                System.out.println("Passed = " + integer);
                if (((Figure) observer).getField().getY() == Field.getInvertedHorizontal().get(secondPosition)){
                    return (Figure) observer;
                }
            }else {
                if (((Figure) observer).getField().getX() == Field.getInvertedVertical().get(integer)){
                    return (Figure) observer;
                }
            }
        }
        throw new RuntimeException("Could not choose exact figure. Turn must be wrong written. Turn = " + mainTurn);
    }

    private Field parseTargetField(String turn){
        int x;
        int y;
        if (!turn.equalsIgnoreCase(shortCastling) && !turn.equalsIgnoreCase(longCastling)){
            if (turn.contains(PLUS)){
                x = Field.getInvertedVertical().get(Character.getNumericValue(turn.charAt(turn.length()-2)));
                y = Field.getInvertedHorizontal().get(turn.charAt(turn.length()-3));
            }else {
                x = Field.getInvertedVertical().get(Character.getNumericValue(turn.charAt(turn.length()-1)));
                y = Field.getInvertedHorizontal().get(turn.charAt(turn.length()-2));
            }
            return new Field(x,y);
        }else {
            log.debug("Target field is null. Castling");
            return null;
        }
    }

    private boolean whetherWrittenTurnIsTransformation(){
        int lengthOfTheWrittenTurn = mainTurn.length();
        if (mainTurn.contains(PLUS)){
            char previousBeforeTheLast = mainTurn.charAt(lengthOfTheWrittenTurn - 2);
            if (FIGURES_IN_WRITTEN_STYLE.contains(Character.toString(previousBeforeTheLast))){
                return true;
            }
        }else {
            char theLast = mainTurn.charAt(lengthOfTheWrittenTurn - 1);
            if (FIGURES_IN_WRITTEN_STYLE.contains(Character.toString(theLast))){
                return true;
            }
        }
        return false;
    }

    public Turn createTurn(List<Tuple2<Figure, Field>> figureToField, Figure figureFromTransformation,
                                  String writtenStyle, boolean isEating, boolean transformation, boolean enPassant,
                                  Figure targetedFigure, int numberOfTurn){
        return Turn.builder().figureToDestinationField(figureToField)
                .figureFromTransformation(figureFromTransformation)
                .writtenStyle(writtenStyle)
                .eating(isEating)
                .transformation(transformation)
                .enPassant(enPassant)
                .targetedFigure(targetedFigure)
                .numberOfTurn(numberOfTurn)
                .build();
    }

    public Figure createFigure(Field field, String writtenStyleOfTheFigure, Color color){
        switch (writtenStyleOfTheFigure){
            case "Q" : return new Queen(field, color);
            case "B" : return new Bishop(field, color);
            case "N" : return new Knight(field, color);
            case "R" : return new Rock(field, color);
            default: return null;
        }
    }

    public Set<Field> fieldsBetweenRockAndKing(final King king, final Field rockPosition){
        Set<Field> fieldsBetween = new HashSet<>();
        if (king.getField().getX() == rockPosition.getX()){
            if (king.getField().getX() > rockPosition.getX()){
                for (int i = king.getField().getX() - 1; i > rockPosition.getX(); i--){
                    fieldsBetween.add(new Field(i, king.getField().getY()));
                }
            }else {
                for (int i = king.getField().getX() + 1; i < rockPosition.getX(); i++){
                    fieldsBetween.add(new Field(i, king.getField().getY()));
                }
            }
        }
        if (king.getField().getY() == rockPosition.getY()){
            if (king.getField().getY() > rockPosition.getY()){
                for (int j = king.getField().getY() - 1; j > rockPosition.getY(); j--){
                    fieldsBetween.add(new Field(king.getField().getX(), j));
                }
            }else {
                for (int j = king.getField().getY() + 1; j < rockPosition.getY(); j++){
                    fieldsBetween.add(new Field(king.getField().getX(), j));
                }
            }
        }
        return fieldsBetween;
    }

    public Set<Field> fieldsBetweenBishopAndKing(final King king, final Field bishopPosition){
        Set<Field> fieldsBetween =  new HashSet<>();
        if (king.getField().getX() > bishopPosition.getX() && king.getField().getY() > bishopPosition.getY()){
            int yPosition = king.getField().getY() - 1;
            for (int i = king.getField().getX() - 1; i > bishopPosition.getX(); i--){
                fieldsBetween.add(new Field(i, yPosition));
                yPosition--;
            }
        }
        if (king.getField().getX() > bishopPosition.getX() && king.getField().getY() < bishopPosition.getY()){
            int yPosition = king.getField().getY() + 1;
            for (int i = king.getField().getX() - 1; i > bishopPosition.getX(); i--){
                fieldsBetween.add(new Field(i, yPosition));
                yPosition++;
            }
        }
        if (king.getField().getX() < bishopPosition.getX() && king.getField().getY() > bishopPosition.getY()){
            int yPosition = king.getField().getY() - 1;
            for (int i = king.getField().getX() + 1; i < bishopPosition.getX(); i++){
                fieldsBetween.add(new Field(i, yPosition));
                yPosition--;
            }

        }
        if (king.getField().getX() < bishopPosition.getX() && king.getField().getY() < bishopPosition.getY()){
            int yPosition = king.getField().getY() + 1;
            for (int i = king.getField().getX() + 1; i < bishopPosition.getX(); i++){
                fieldsBetween.add(new Field(i, yPosition));
                yPosition++;
            }
        }
        return fieldsBetween;
    }

    public Set<Field> fieldsBetweenQueenAndKing(final King king, final Field queenPosition){
        Set<Field> fieldsBetweenQueenAndKing = new HashSet<>();
        if (king.getField().getX() == queenPosition.getX() || king.getField().getY() == queenPosition.getY()){
            fieldsBetweenQueenAndKing.addAll(fieldsBetweenRockAndKing(king, queenPosition));
        }else {
            fieldsBetweenQueenAndKing.addAll(fieldsBetweenBishopAndKing(king, queenPosition));
        }
        return fieldsBetweenQueenAndKing;
    }

    public Set<Figure> getAffectedFigures(Color color){
        Set<Figure> acceptedFigures = new HashSet<>();
        List<Observer> observers = board.getFigures(color);
        affectedFields.forEach(f -> {
            observers.forEach(o -> {
                if (((Figure)o).getAttackedFields().contains(f)){
                    acceptedFigures.add((Figure)o);
                }
            });
        });
        return acceptedFigures;
    }

    private void getAffectedFields(Turn turn){
        affectedFields = new HashSet<>();
        for (Tuple2<Figure, Field> tuple2 : turn.getFigureToDestinationField()){
            affectedFields.add(tuple2._1.getField());
            affectedFields.add(tuple2._2);
        }
    }

    public void makeTurn(Turn turn){
        getAffectedFields(turn);
        setTurnForUndoing(turn);
        for (Tuple2<Figure, Field> tuple2 : turn.getFigureToDestinationField()){
            board.setNewCoordinates(turn, tuple2._1, tuple2._2, turn.getTargetedFigure(), false, turn.isEnPassant());
        }
        makePullAdditionalAlliesAndEnemies();
    }

    public void undoTurn(Turn turn){
        Turn undoTurn = Turn.builder()
                .figureToDestinationField(tuplesFigureToField)
                .eating(false)
                .writtenStyle("")
                .numberOfTurn(turn.getNumberOfTurn())
                .build();
        for (Tuple2<Figure, Field> tuple2 : undoTurn.getFigureToDestinationField()){
            board.setNewCoordinates(turn, tuple2._1, tuple2._2, undoTurn.getTargetedFigure(), true, turn.isEnPassant());
        }
        ProcessingUtils.eatenFigureToResurrection = null;
        makePullAdditionalAlliesAndEnemies();
    }

    private void setTurnForUndoing(Turn turn){
        tuplesFigureToField = new ArrayList<>();
        eatenFigureToResurrection = null;
        for (Tuple2<Figure, Field> tuple2 : turn.getFigureToDestinationField()){
            tuplesFigureToField.add(new Tuple2<>(tuple2._1, tuple2._1.getField()));
        }
        if (turn.isEating()){
            if (turn.getFigureToDestinationField().size() == 1 && turn.getFigureToDestinationField().get(0)._1.getClass() == Pawn.class
                    && ((Pawn) turn.getFigureToDestinationField().get(0)._1).isEnPassant()){


            }else {
                Figure tempFigure = board.getFieldToFigure().get(turn.getFigureToDestinationField().get(0)._2);
                System.out.println("temp figure = " + tempFigure);
                eatenFigureToResurrection = tempFigure.createNewFigure();
//            System.out.println("Eaten figure = " + eatenFigureToResurrection);
            }
        }
        figureBornFromTransformation = turn.getFigureFromTransformation();
    }

    private void makePullAdditionalAlliesAndEnemies(){
        Map<Figure, Set<Figure>> figureToChosenAllies = new HashMap<>();
        board.getFigures().forEach(f -> {
            Set<Figure> chosenAllies = ((Figure)f).pullAdditionalAlliesAndEnemies();
            if (!isEmpty(chosenAllies)){
                figureToChosenAllies.put((Figure)f, chosenAllies);
            }
        });
        for (int i = 0; i < SIZE; i++){
            for (Figure curFigure : figureToChosenAllies.keySet()){
                for (Figure ally : figureToChosenAllies.get(curFigure)){
                    if (ally != null){
                        doUpdate(curFigure, ally);
                    }
                }
            }
        }
    }

    private void doUpdate(Figure curFigure, Figure ally){
        for (Figure undefendedAlly : ally.getAlliesIProtect()){
            updateProtectionOfUndefendedAllies(curFigure, ally, undefendedAlly);
        }
        for (Figure prey : ally.getWhoCouldBeEaten()){
            updateWhoCouldBeEaten(curFigure, ally, prey);
        }
    }

    private void updateWhoCouldBeEaten(Figure curFigure, Figure ally, Figure prey){
        if (!curFigure.getWhoCouldBeEaten().contains(prey) && ally.getWhoCouldBeEaten().contains(prey)
                && curFigure.getAttackedFields().contains(prey.getField()) && isOnTheSameLine(curFigure, ally, prey)){
            curFigure.getWhoCouldBeEaten().add(prey);
            prey.addEnemy(curFigure);
        }
    }

    private void updateProtectionOfUndefendedAllies(Figure curFigure, Figure ally, Figure undefendedAlly){
        if (!curFigure.getAlliesIProtect().contains(undefendedAlly) && ally.getAlliesIProtect().contains(undefendedAlly)
                && curFigure.getAttackedFields().contains(undefendedAlly.getField()) && isOnTheSameLine(curFigure, ally, undefendedAlly)
                && !curFigure.equals(undefendedAlly) && !ally.equals(undefendedAlly)){
            curFigure.addAllyIProtect(undefendedAlly);
            undefendedAlly.addAllyProtectMe(curFigure);
        }
    }

    private boolean isOnTheSameLine(Figure f1, Figure f2, Figure f3){
        if (f1.getClass() == Bishop.class || f2.getClass() == Bishop.class  || f3.getClass() == Bishop.class){
            return (abs(f1.getField().getX() - f2.getField().getX()) == abs(f1.getField().getY() - f2.getField().getY()))
                    && (abs(f2.getField().getX() - f3.getField().getX()) == abs(f2.getField().getY() - f3.getField().getY()))
                    && (abs(f1.getField().getX() - f3.getField().getX()) == abs(f1.getField().getY() - f3.getField().getY()));
        }
        if (f1.getClass() == Rock.class || f2.getClass() == Rock.class  || f3.getClass() == Rock.class){
            return  ((f1.getField().getX() == f2.getField().getX()) && (f2.getField().getX() == f3.getField().getX())) ||
                    ((f1.getField().getY() == f2.getField().getY()) && (f2.getField().getY() == f3.getField().getY()));
        }
        return ((f1.getField().getX() == f2.getField().getX()) && (f2.getField().getX() == f3.getField().getX())) ||
                ((f1.getField().getY() == f2.getField().getY()) && (f2.getField().getY() == f3.getField().getY())) ||
                (((abs(f1.getField().getX() - f2.getField().getX()) == abs(f1.getField().getY() - f2.getField().getY()))
                        && (abs(f2.getField().getX() - f3.getField().getX()) == abs(f2.getField().getY() - f3.getField().getY())))
                        && (abs(f1.getField().getX() - f3.getField().getX()) == abs(f1.getField().getY() - f3.getField().getY())));
    }

    private boolean isEnPassantScenario(List<Tuple2<Figure, Field>> figureToField){
        return true;
    }

    public boolean isEmpty(Set<?> set){
        return set == null || set.isEmpty();
    }
}