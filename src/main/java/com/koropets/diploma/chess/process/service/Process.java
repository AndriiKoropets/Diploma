package com.koropets.diploma.chess.process.service;

import com.koropets.diploma.chess.process.constants.Constants;
import com.koropets.diploma.chess.process.dto.FinalResult;
import com.koropets.diploma.chess.process.dto.Parameter;
import com.koropets.diploma.chess.utils.ProcessingUtils;
import com.koropets.diploma.chess.model.Observer;
import com.koropets.diploma.chess.model.Board;
import com.koropets.diploma.chess.model.Color;
import com.koropets.diploma.chess.model.Figure;
import com.koropets.diploma.chess.model.Field;
import com.koropets.diploma.chess.model.Pawn;
import com.koropets.diploma.chess.model.Bishop;
import com.koropets.diploma.chess.model.Knight;
import com.koropets.diploma.chess.model.Rock;
import com.koropets.diploma.chess.model.Queen;
import com.koropets.diploma.chess.model.King;
import com.koropets.diploma.chess.process.dto.Turn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

@Service
public class Process {

    private static final Logger LOG = LoggerFactory.getLogger(Process.class);
//    private final static String PATH_TO_FILE = "src/main/resources/parties/enPassantBlack.txt";
//    private static final String PATH_TO_FILE = "src/main/resources/parties/tetsPartyPawn.txt";
    private final static String PATH_TO_FILE = "src/main/resources/parties/enPassantWhite.txt";
    //1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 14, 15, 16, 17 19, 20 21 are processed properly
    //Two figures could eat at the same time the same enemy:1, 10, 12, 17 - are processed properly
    //Transformation : 6, 7, 13, 18,
    private final static String PATH_TO_DIRECTORY = "src/main/resources/parties/";

    @Autowired
    public Board board;
    @Autowired
    private Game game;
    @Autowired
    private EstimatePosition estimatePosition;
    private static final Pattern pattern = Pattern.compile("^(\\d+)\\.\\s*(\\S+)\\s*(\\S+)*$");
    private static Parameter whiteEstimationWholeParty;
    private static Parameter blackEstimationWholeParty;

    static FinalResult fullWhiteEstimation;
    static FinalResult fullBlackEstimation;

//    public static void main(String[] args){
//        process();
//    }

    public void process(){
        LOG.info("Process is starting");
        System.out.println("Process started");
        whiteEstimationWholeParty = Parameter.builder().build();
        blackEstimationWholeParty = Parameter.builder().build();
        fullWhiteEstimation = FinalResult.builder().build();
        fullBlackEstimation = FinalResult.builder().build();
        System.out.println("Board = " + board);
        board.init();
        printAllBoard();
        File file = null;
        try{
            file = new File(PATH_TO_FILE);
            Scanner scnr = new Scanner(file);
            String sCurrentLine;
            Set<Turn> whitePossibleTurns;
            Set<Turn> blackPossibleTurns;
            while (scnr.hasNextLine()) {
                sCurrentLine = scnr.nextLine();
                Matcher matcher = pattern.matcher(sCurrentLine);
                if (matcher.matches()){
                    int numberOfTurn = Integer.valueOf(matcher.group(1));
                    String writtenWhiteTurn = matcher.group(2);
                    String writtenBlackTurn = matcher.group(3);
                    Turn whiteTurn = ProcessingUtils.getActualTurn(writtenWhiteTurn, Color.WHITE, numberOfTurn);
                    System.out.println("White turn = " + whiteTurn);
                    whitePossibleTurns = game.getPossibleTurnsAndEatings(Color.WHITE, numberOfTurn);
                    printAllPossibleTurns(whitePossibleTurns);
                    //TODO write logic which gets rid of makeTurn. It should be monolithic. Whole estimation could be defined in EstimatePosition class.
                    board.setCurrentTurn(whiteTurn);
                    ProcessingUtils.makeTurn(whiteTurn);
//                    System.out.println("After turn = " + whiteTurn);
                    printAllBoard();
//                    currentStateOfAllFigures();
                    whiteEstimationWholeParty = estimatePosition.estimate(whiteTurn, whitePossibleTurns, Color.WHITE);

                    fullWhiteEstimation = countFullEstimation(whiteEstimationWholeParty, Color.WHITE);
                    if (writtenBlackTurn != null){
                        Turn blackTurn = ProcessingUtils.getActualTurn(writtenBlackTurn, Color.BLACK, numberOfTurn);
                        System.out.println("Black turn = " + blackTurn);
                        blackPossibleTurns = game.getPossibleTurnsAndEatings(Color.BLACK, numberOfTurn);
                        printAllPossibleTurns(blackPossibleTurns);
                        board.setCurrentTurn(blackTurn);
                        ProcessingUtils.makeTurn(blackTurn);
                        printAllBoard();
//                        System.out.println("After turn = " + blackTurn);
                        System.out.println("Figures = " + board.getFigures());
                        System.out.println("White figures = " + board.getFigures(Color.WHITE));
                        System.out.println("Black figures = " + board.getFigures(Color.BLACK));
//                        System.out.println("Size = " + Board.getTakenFields().size() + "Taken fields = " + Board.getTakenFields());
                        blackEstimationWholeParty = estimatePosition.estimate(blackTurn, blackPossibleTurns, Color.BLACK);

                        fullBlackEstimation = countFullEstimation(blackEstimationWholeParty, Color.BLACK);
                    }
//                    printAllBoard();
//                    currentStateOfAllFigures();
                }
                System.out.println("White estimation = " + whiteEstimationWholeParty);
                System.out.println("Black estimation = " + blackEstimationWholeParty);
                System.out.println("White figures = " + board.getFigures(Color.WHITE));
                System.out.println("Black figures = " + board.getFigures(Color.BLACK));
            }
            printAllBoard();
//            System.out.println("White estimation = " + whiteEstimationWholeParty);
//            System.out.println("Black estimation = " + blackEstimationWholeParty);
            System.out.println("Full estimation");
            System.out.println("White = " + fullWhiteEstimation);
            System.out.println("Black = " + fullBlackEstimation);
        } catch (IOException e) {
            LOG.info("File {} was not found", file);
            throw new RuntimeException();
        }
    }

    private void currentStateOfAllFigures(){
        System.out.println("White figures");
        for (Observer observer : board.getFigures(Color.WHITE)){
            Figure currentFigure = (Figure) observer;
            printInfoAboutFigure(currentFigure);
        }
        System.out.println("Black figures");
        for (Observer observer : board.getFigures(Color.BLACK)){
            Figure currentFigure = (Figure) observer;
            printInfoAboutFigure(currentFigure);
        }
    }

    private void printAllPossibleTurns(Set<Turn> allPossibleTurns){
        System.out.println("Size = " + allPossibleTurns.size());
        for (Turn possibleTurn : allPossibleTurns){
            System.out.println("Turn = " + possibleTurn.getFigureToDestinationField());
        }
    }

    private void printInfoAboutFigure(Figure currentFigure){
        System.out.println(currentFigure);
//        System.out.println("Possible fields to move = " + currentFigure.getPossibleFieldsToMove());
        System.out.println("Who could be eaten previous state = " + currentFigure.getWhoCouldBeEatenPreviousState());
        System.out.println("Who could be eaten now = " + currentFigure.getWhoCouldBeEaten());
        System.out.println("Get allies I protect = " + currentFigure.getAlliesIProtect());
        System.out.println("Get allies protect me = " + currentFigure.getAlliesProtectMe());
        System.out.println("Get figures attack me = " + currentFigure.getEnemiesAttackMe());
    }

    private void printAllBoard(){
        System.out.println();
        int counter = 1;
        for (int i = 0; i < Constants.SIZE; i++){
            System.out.print(Field.getVertical().get(i) + "  ");
            for (int j = 0; j < Constants.SIZE; j++){
                Field currentPoint = new Field(i, j);
                if (currentPoint.isTaken()){
                    System.out.print(" " + printFigure(board.getFieldToFigure().get(currentPoint)) + " ");
                }else {
                    System.out.print("   ");
                }
            }
            if (counter == Constants.SIZE){
                System.out.println();
                System.out.println();
                System.out.print("    ");
                for (int k = 0; k < Constants.SIZE; k++){
                    System.out.print(Field.getHorizontal().get(k) + "  ");
                }
            }
            counter++;
            System.out.println();
        }
    }

    private String printFigure(Figure figure){
        if (figure.getClass() == Pawn.class){
            return figure.getColor() == Color.WHITE ? "P" : "p";
        }
        if (figure.getClass() == Rock.class){
            return figure.getColor() == Color.WHITE ? "R" : "r";
        }
        if (figure.getClass() == Knight.class){
            return figure.getColor() == Color.WHITE ? "N" : "n";
        }
        if (figure.getClass() == Bishop.class){
            return figure.getColor() == Color.WHITE ? "B" : "b";
        }
        if (figure.getClass() == King.class){
            return figure.getColor() == Color.WHITE ? "K" : "k";
        }
        if (figure.getClass() == Queen.class){
            return figure.getColor() == Color.WHITE ? "Q" : "q";
        }
        return null;
    }

    private FinalResult countFullEstimation(Parameter parameter, Color color){
        FinalResult globalEstimation = (color == Color.BLACK) ? fullBlackEstimation : fullWhiteEstimation;
        return FinalResult.builder().first(globalEstimation.getFirst() + parameter.getFirstAttackEnemy())
                .second(globalEstimation.getSecond() + parameter.getSecondBeUnderAttack())
                .third(globalEstimation.getThird() + parameter.getThirdWithdrawAttackOnEnemy())
                .fourth(globalEstimation.getFourth() + parameter.getFourthWithdrawAttackOnMe())
                .fifth(globalEstimation.getFifth() + parameter.getFifthDontTakeAChanceToAttack()._1)
                .sixth(globalEstimation.getSixth() + parameter.getSixthDontTakeAChanceToBeUnderAttack()._1)
                .seventh(globalEstimation.getSeventh() + parameter.getSeventhDontTakeAChanceToWithdrawAttackOnEnemy()._1)
                .eighth(globalEstimation.getEighth() + parameter.getEighthDontTakeAChanceToWithdrawAttackOnMe()._1)
                .build();
    }
}