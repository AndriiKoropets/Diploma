package com.koropets.diploma.chess.process.service;

import com.koropets.diploma.chess.model.Board;
import com.koropets.diploma.chess.model.Color;
import com.koropets.diploma.chess.process.dto.AntiParameter;
import com.koropets.diploma.chess.process.dto.Parameter;
import com.koropets.diploma.chess.utils.ProcessingUtils;
import com.koropets.diploma.chess.model.Observer;
import com.koropets.diploma.chess.model.Figure;
import com.koropets.diploma.chess.model.Field;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import com.koropets.diploma.chess.process.dto.Turn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

@Service
public class EstimatePosition {

    private static Color whoseTurn;

    @Autowired
    private Board board;

    Parameter estimate(Turn turn, Set<Turn> possibleTurns, Color side){
        whoseTurn = side;
        int firstParam = estimateFirstParameter();
        int secondParam = estimateSecondParameter();
        int thirdParam = estimateThirdParameter();
        int fourthParam = estimateFourthParameter();
        List<Tuple2<Turn, AntiParameter>> turnToAntiParameter = estimateAntiParameter(turn, possibleTurns);
        Tuple2<Turn, Integer> estimatedFifthParameter = estimateFifthParameter(turnToAntiParameter);
        Tuple2<Turn, Integer> estimatedSixthParameter = estimateSixthParameter(turnToAntiParameter);
        Tuple2<Turn, Integer> estimatedSeventhParameter = estimateSeventhParameter(turnToAntiParameter);
        Tuple2<Turn, Integer> estimatedEighthParameter = estimateEightParameter(turnToAntiParameter);
        Tuple2<Integer, List<Tuple2<Figure, Field>>> fifthParamToInvolvedFigures = new Tuple2<>(estimatedFifthParameter._2 - firstParam, estimatedFifthParameter._1.getFigureToDestinationField());
        Tuple2<Integer, List<Tuple2<Figure, Field>>> sixthParamToInvolvedFigures = new Tuple2<>(estimatedSixthParameter._2 - secondParam, estimatedSixthParameter._1.getFigureToDestinationField());
        Tuple2<Integer, List<Tuple2<Figure, Field>>> seventhParamToInvolvedFigures = new Tuple2<>(estimatedSeventhParameter._2 - thirdParam, estimatedSeventhParameter._1.getFigureToDestinationField());
        Tuple2<Integer, List<Tuple2<Figure, Field>>> eighthParamToInvolvedFigures = new Tuple2<>(estimatedEighthParameter._2 - fourthParam, estimatedEighthParameter._1.getFigureToDestinationField());
        ProcessingUtils.makeTurn(turn);
        return Parameter.builder()
                .firstAttackEnemy(firstParam)
                .secondBeUnderAttack(secondParam)
                .thirdWithdrawAttackOnEnemy(thirdParam)
                .fourthWithdrawAttackOnMe(fourthParam)
                .fifthDontTakeAChanceToAttack(fifthParamToInvolvedFigures)
                .sixthDontTakeAChanceToBeUnderAttack(sixthParamToInvolvedFigures)
                .seventhDontTakeAChanceToWithdrawAttackOnEnemy(seventhParamToInvolvedFigures)
                .eighthDontTakeAChanceToWithdrawAttackOnMe(eighthParamToInvolvedFigures)
                .build();
    }

    private Tuple2<Turn, Integer> estimateEightParameter(final List<Tuple2<Turn, AntiParameter>> turnAntiParameterMap) {
        int max = 0;
        Turn turnOfTheMaxParam = null;
        for (Tuple2<Turn, AntiParameter> curTuple : turnAntiParameterMap){
            int paramPerTurn = curTuple._2.getEighthParam();
            if (paramPerTurn >= max){
                max = paramPerTurn;
                turnOfTheMaxParam = curTuple._1;
            }
        }
        if (turnOfTheMaxParam == null){
            throw new RuntimeException("Unexpected exception during estimating eighth parameter");
        }
        return new Tuple2<>(turnOfTheMaxParam, max);
    }

    private Tuple2<Turn, Integer> estimateSeventhParameter(final List<Tuple2<Turn, AntiParameter>> turnAntiParameterMap) {
        int max = 0;
        Turn turnOfTheMaxParam = null;
        for (Tuple2<Turn, AntiParameter> curTuple : turnAntiParameterMap){
            int paramPerTurn = curTuple._2.getSeventhParam();
            if (paramPerTurn >= max){
                max = paramPerTurn;
                turnOfTheMaxParam = curTuple._1;
            }
        }
        if (turnOfTheMaxParam == null){
            throw new RuntimeException("Unexpected exception during estimating seventh parameter");
        }
        return new Tuple2<>(turnOfTheMaxParam, max);
    }

    private Tuple2<Turn, Integer> estimateSixthParameter(final List<Tuple2<Turn, AntiParameter>> turnAntiParameterMap) {
        int max = 0;
        Turn turnOfTheMaxParam = null;
        for (Tuple2<Turn, AntiParameter> curTuple2 : turnAntiParameterMap){
            int paramPerTurn = curTuple2._2.getSixthParam();
            if (paramPerTurn >= max){
                max = paramPerTurn;
                turnOfTheMaxParam = curTuple2._1;
            }
        }
        if (turnOfTheMaxParam == null){
            throw new RuntimeException("Unexpected exception during estimating sixth parameter");
        }
        return new Tuple2<>(turnOfTheMaxParam, max);
    }

    private Tuple2<Turn, Integer> estimateFifthParameter(final List<Tuple2<Turn, AntiParameter>> turnAntiParameterMap) {
        int max = 0;
        Turn turnOfTheMaxParam = null;
        for (Tuple2<Turn, AntiParameter> curTuple : turnAntiParameterMap){
            int paramPerTurn = curTuple._2.getFifthParam();
            if (paramPerTurn >= max){
                max = paramPerTurn;
                turnOfTheMaxParam = curTuple._1;
            }
        }
        if (turnOfTheMaxParam == null){
            throw new RuntimeException("Unexpected exception during estimating fifth parameter");
        }
        return new Tuple2<>(turnOfTheMaxParam, max);
    }

    private List<Tuple2<Turn, AntiParameter>> estimateAntiParameter(final Turn turn, final Set<Turn> possibleTurns){
        ProcessingUtils.undoTurn(turn);
        List<Tuple2<Turn, AntiParameter>> turnAntiParameterMap = new ArrayList<>();
        for (Turn posTurn : possibleTurns){
            if (!possibleTurns.equals(turn)){
                ProcessingUtils.makeTurn(posTurn);
                AntiParameter antiParameter = AntiParameter.builder()
                        .fifthParam(estimateFirstParameter())
                        .sixthParam(estimateSecondParameter())
                        .seventhParam(estimateThirdParameter())
                        .eighthParam(estimateFourthParameter())
                        .build();
                turnAntiParameterMap.add(new Tuple2<>(posTurn, antiParameter));
                ProcessingUtils.undoTurn(posTurn);
            }
        }
        return turnAntiParameterMap;
    }

    private int estimateFourthParameter() {
        List<Observer> enemies = (whoseTurn == Color.WHITE) ? board.getFigures(Color.BLACK) : board.getFigures(Color.WHITE);
        return calculateWithdrawingAttackAndBeUnderAttack(enemies);
    }

    private int estimateThirdParameter() {
        List<Observer> alliesObservers = board.getFigures(whoseTurn);
        return calculateWithdrawingAttackAndBeUnderAttack(alliesObservers);
    }

    private int estimateSecondParameter(){
        List<Observer> enemies = (whoseTurn == Color.WHITE) ? board.getFigures(Color.BLACK) : board.getFigures(Color.WHITE);
        return calculateAttackAndBeUnderAttack(enemies);
    }

    private int estimateFirstParameter(){
        List<Observer> chosenFigures = board.getFigures(whoseTurn);
        return calculateAttackAndBeUnderAttack(chosenFigures);
    }

    private int calculateAttackAndBeUnderAttack(List<Observer> figures){
        int param = 0;
        Set<Figure> chosenFigures = new HashSet<>();
        figures.forEach(o -> {
            if (isPreysBecameBigger(((Figure)o).getWhoCouldBeEatenPreviousState(), ((Figure)o).getWhoCouldBeEaten())){
                chosenFigures.add(((Figure)o));
            }
        });
        for (Figure curFigure : chosenFigures){
            for (Figure prey : curFigure.getWhoCouldBeEaten()){
                if (!curFigure.getWhoCouldBeEatenPreviousState().contains(prey) &&
                        (prey.getEnemiesAttackMe().size() >= prey.getAlliesProtectMe().size() ||
                                curFigure.getValue() < prey.getValue())){
                    param += prey.getPoint();
                }
            }
        }
        return param;
    }

    private int calculateWithdrawingAttackAndBeUnderAttack(List<Observer> figures){
        int param = 0;
        Set<Figure> chosenFigures = new HashSet<>();
        figures.forEach(o -> {
            if (isPreysBecameSmaller(((Figure)o).getWhoCouldBeEatenPreviousState(), ((Figure)o).getWhoCouldBeEaten())){
                chosenFigures.add(((Figure)o));
            }
        });
        for (Figure curFigure : chosenFigures){
            for (Figure prevPrey : curFigure.getWhoCouldBeEatenPreviousState()){
                if (!curFigure.getWhoCouldBeEaten().contains(prevPrey)){
                    param += prevPrey.getPoint();
                }
            }
        }
        return param;
    }

    private boolean isPreysBecameBigger(Set<Figure> previousPreys, Set<Figure> curPreys){
        for (Figure curPrey : curPreys){
            if (!previousPreys.contains(curPrey)){
                return true;
            }
        }
        return false;
    }

    private boolean isPreysBecameSmaller(Set<Figure> previousPreys, Set<Figure> curPreys){
        for (Figure prevPrey : previousPreys){
            if (!curPreys.contains(prevPrey)) {
                return true;
            }
        }
        return false;
    }

//     private static int secParamSubstitutionViaFigure(final Turn turn){
//        int param = 0;
//        Figure figure = retrieveFigureFromTurn(turn);
//         Set<Observer> enemies = (whoseTurn == Color.BLACK) ? Board.getFigures(Color.WHITE) : Board.getFigures(Color.BLACK);
//
//        return param;
//     }
//
//    private static int secParamActualSubstitution(final Turn turn) {
//        int parameter = 0;
//        Figure figure = retrieveFigureFromTurn(turn);
//        Set<Observer> enemies = (whoseTurn == Color.BLACK) ? Board.getFigures(Color.WHITE) : Board.getFigures(Color.BLACK);
//        for (Observer enemyObserver : enemies){
//            Figure enemy = ((Figure) enemyObserver);
//            for (Figure prey : enemy.getWhoCouldBeEaten()){
//                if (prey.equals(figure)){
//                    parameter += figure.getPoint();
//                }
//            }
//        }
//        return parameter;
//    }
//
//    private static Figure retrieveFigureFromTurn(final Turn turn){
//        if (turn.getFigures().size() == 1){
//            return turn.getFigures().keySet().iterator().next();
//        }
//        for (Figure temp : turn.getFigures().keySet()){
//            if (temp.getClass() == Rock.class){
//                return  temp;
//            }
//        }
//        throw new RuntimeException("Could not choose figure from actual turn");
//    }
//
//    private static int firstParamAttackOthersAllies(Turn turn){
//        int parameter = 0;
//        Set<Figure> acceptedFigures = ProcessingUtils.getAffectedFigures(whoseTurn);
//        System.out.println("AF = " + acceptedFigures);
//        for (Figure f : turn.getFigures().keySet()){
//            acceptedFigures.remove(f);
//        }
////        acceptedFigures = acceptedFigures.stream().filter(f -> !turn.getFigures().keySet().contains(f)).collect(Collectors.toSet());
//        System.out.println("Accepted figures = " + acceptedFigures);
//        Set<Figure> firstLineFigure = new HashSet<>();
//        for (Figure f : acceptedFigures){
//            if (isCollectionsChanged(f) && !turn.getFigures().keySet().contains(f)){
//                firstLineFigure.add(f);
//            }
//        }
//        System.out.println("First line = " + firstLineFigure);
//        Map<Figure, Set<Figure>> figureToItsPreys = new HashMap<>();
//
//        for (Figure f : firstLineFigure){
//            Set<Figure> preys = appearedPreys(f);
//            figureToItsPreys.put(f, preys);
//        }
//
//        System.out.println("Figures to preys = " + figureToItsPreys);
//
//
//        for (Figure curFigure : figureToItsPreys.keySet()){
//            for (Figure prey : figureToItsPreys.get(curFigure)){
//                for (Figure ally : curFigure.getAlliesIProtect()){
//                    System.out.println("ally - " + ally);
//                    updateWhoCouldBeEaten(ally, curFigure, prey);
//                }
//            }
//        }
//
//        for (Figure f : acceptedFigures){
//            System.out.println( f + "Before = " + f.getWhoCouldBeEatenPreviousState());
//            System.out.println( f + "Now ==== " + f.getWhoCouldBeEaten());
//        }
//
//        System.out.println("Accepted figures second time = " + acceptedFigures);
//
//        for (Figure f : acceptedFigures){
//            System.out.println("Figure = " + f);
//            for (Figure prey : f.getWhoCouldBeEaten()){
//                System.out.println("Prey - " + prey);
//                if (!f.getWhoCouldBeEatenPreviousState().contains(prey)
//                        && prey.getEnemiesAttackMe().size() >= prey.getAlliesProtectMe().size()
//                        && !turn.getFigures().keySet().contains(f)){
//                    parameter += prey.getPoint();
//                }
//            }
//        }
//        System.out.println("Parameter from allies = " + parameter);
//        return parameter;
//    }
//
//    private static int firstParamAttackViaAlly(Turn turn) {
//        int parameter = 0;
//        for (Figure curFigure : turn.getFigures().keySet()){
//            if (curFigure.getClass() == Queen.class){
//                System.out.println(curFigure.getAttackedFields());
//            }
//            for (Figure ally : curFigure.getAlliesIProtect()){
////                System.out.println("Ally = " + ally);
//                for (Figure prey : ally.getWhoCouldBeEaten()){
//                    Field preyField = prey.getField();
////                    System.out.println("PreyField = " + preyField + " for such ally" + ally);
//                    if (curFigure.getAttackedFields().contains(preyField) && !curFigure.getPreyField().contains(preyField)
//                            && isOnTheSameLine(curFigure, ally, prey)){
//                        curFigure.getWhoCouldBeEaten().add(prey);
//                        parameter += prey.getPoint();
////                        System.out.println("Parameter = " + parameter);
//                    }
//                }
//            }
//        }
//        System.out.println("Parameter via = " + parameter);
//        return parameter;
//    }
//
////    private static boolean isOnTheSameLine(Figure f1, Figure f2, Figure f3){
////        return ((f1.getField().getX() == f2.getField().getX()) && (f2.getField().getX() == f3.getField().getX())) ||
////                ((f1.getField().getY() == f2.getField().getY()) && (f2.getField().getY() == f3.getField().getY())) ||
////                (((abs(f1.getField().getX() - f2.getField().getX()) == abs(f1.getField().getY() - f2.getField().getY()))
////                        && (abs(f2.getField().getX() - f3.getField().getX()) == abs(f2.getField().getY() - f3.getField().getY())))
////                        && (abs(f1.getField().getX() - f3.getField().getX()) == abs(f1.getField().getY() - f3.getField().getY())));
////    }
//
//    private static boolean isCollectionsChanged(Figure figure){
//        return !figure.getWhoCouldBeEatenPreviousState().containsAll(figure.getWhoCouldBeEaten());
//    }
//
////    private static void updateWhoCouldBeEaten(Figure curFigure, Figure ally, Figure prey){
////        if (!curFigure.getWhoCouldBeEaten().contains(prey) && ally.getWhoCouldBeEaten().contains(prey)
////                && curFigure.getAttackedFields().contains(prey.getField()) && isOnTheSameLine(curFigure, ally, prey)){
////            curFigure.getWhoCouldBeEaten().add(prey);
////            prey.addEnemy(curFigure);
////            for (Figure f : curFigure.getAlliesIProtect()){
////                if (!f.equals(ally)){
////                    updateWhoCouldBeEaten(f, curFigure, prey);
////                }
////            }
////        }
////    }
//
//    private static Set<Figure> appearedPreys(final Figure curFigure){
//        Set<Figure> appearedPreys = new HashSet<>();
//        Set<Figure> preysBefore = curFigure.getWhoCouldBeEatenPreviousState();
//        Set<Figure> preysNow = curFigure.getWhoCouldBeEaten();
//        for (Figure f : preysNow){
//            if (!preysBefore.contains(f)){
//                appearedPreys.add(f);
//            }
//        }
//        return appearedPreys;
//    }
//
//    private static int firstParamActualAttack(final Turn turn) {
//        int parameter = 0;
//
//        for (Figure figure : turn.getFigures().keySet()){
////            System.out.println("Figure = " + figure);
////            System.out.println("All attacked fields = " + figure.getAttackedFields());
////            System.out.println("Where to move = " + figure.getPossibleFieldsToMove());
////            System.out.println("Previous preys = " + figure.getWhoCouldBeEatenPreviousState());
////            System.out.println("Current preys = " + figure.getWhoCouldBeEaten());
////            System.out.println("All allies protect me = " + figure.getAlliesProtectMe());
////            System.out.println("Allies I protect = " + figure.getAlliesIProtect());
////            System.out.println("Prey's fields = " + figure.getPreyField());
//            for (Figure prey : figure.getWhoCouldBeEaten()){
//                if (prey.getEnemiesAttackMe().size() >= prey.getAlliesProtectMe().size() || prey.getValue() > figure.getValue()){
//                    parameter += prey.getPoint();
//                }
//            }
//        }
//        System.out.println("Parameter actual = " + parameter);
//        return parameter;
//    }
//
}
