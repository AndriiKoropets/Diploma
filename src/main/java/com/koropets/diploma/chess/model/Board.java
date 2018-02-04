package com.koropets.diploma.chess.model;

import com.koropets.diploma.chess.process.dto.Turn;
import com.koropets.diploma.chess.utils.ProcessingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import static com.koropets.diploma.chess.process.constants.Constants.SIZE;

/**
 * @author AndriiKoropets
 */
@Component
@Slf4j
public class Board implements Subject{

    private  List<Observer> figures = new ArrayList<>();
    private  List<Observer> whiteFigures = new ArrayList<Observer>();
    private  List<Observer> blackFigures = new ArrayList<>();
    private  Set<Field> fieldsUnderWhiteInfluence = new LinkedHashSet<Field>();
    private  Set<Field> fieldsUnderBlackInfluence = new LinkedHashSet<Field>();
    private  List<Turn> possibleTurnsAndKillings = new ArrayList<Turn>();
    private  final Set<Field> takenFields = new LinkedHashSet<Field>();
    private  final Map<Field, Figure> fieldToFigure = new HashMap<Field, Figure>();
    private Field field;
    private  Turn previousTurn;
    private  Turn currentTurn;
    private int turnNumber;

//    @PostConstruct
    public void init(){
        //Putting white figures on the board
        Figure whitePawnA = new Pawn(new Field(6,0), Color.WHITE);
        Figure whitePawnB = new Pawn(new Field(6,1), Color.WHITE);
        Figure whitePawnC = new Pawn(new Field(6,2), Color.WHITE);
        Figure whitePawnD = new Pawn(new Field(6,3), Color.WHITE);
        Figure whitePawnE = new Pawn(new Field(6,4), Color.WHITE);
        Figure whitePawnF = new Pawn(new Field(6,5), Color.WHITE);
        Figure whitePawnG = new Pawn(new Field(6,6), Color.WHITE);
        Figure whitePawnH = new Pawn(new Field(6,7), Color.WHITE);
        Figure whiteRockA = new Rock(new Field(7,0), Color.WHITE);
        Figure whiteRockH = new Rock(new Field(7,7), Color.WHITE);
        Figure whiteKnightB = new Knight(new Field(7,1), Color.WHITE);
        Figure whiteKnightG = new Knight(new Field(7,6), Color.WHITE);
        Figure whiteBishopC = new Bishop(new Field(7,2), Color.WHITE);
        Figure whiteBishopF = new Bishop(new Field(7,5), Color.WHITE);
        Figure whiteQueen = new Queen(new Field(7,3), Color.WHITE);
        Figure whiteKing = new King(new Field(7,4), Color.WHITE);
        //Adding to figures collection
        register(whitePawnA);
        register(whitePawnB);
        register(whitePawnC);
        register(whitePawnD);
        register(whitePawnE);
        register(whitePawnF);
        register(whitePawnG);
        register(whitePawnH);
        register(whiteRockA);
        register(whiteRockH);
        register(whiteKnightB);
        register(whiteKnightG);
        register(whiteBishopC);
        register(whiteBishopF);
        register(whiteQueen);
        register(whiteKing);
        //Putting black figures on the board
        Figure blackPawnA = new Pawn(new Field(1,0), Color.BLACK);
        Figure blackPawnB = new Pawn(new Field(1,1), Color.BLACK);
        Figure blackPawnC = new Pawn(new Field(1,2), Color.BLACK);
        Figure blackPawnD = new Pawn(new Field(1,3), Color.BLACK);
        Figure blackPawnE = new Pawn(new Field(1,4), Color.BLACK);
        Figure blackPawnF = new Pawn(new Field(1,5), Color.BLACK);
        Figure blackPawnG = new Pawn(new Field(1,6), Color.BLACK);
        Figure blackPawnH = new Pawn(new Field(1,7), Color.BLACK);
        Figure blackRockA = new Rock(new Field(0,0), Color.BLACK);
        Figure blackRockH = new Rock(new Field(0,7), Color.BLACK);
        Figure blackKnightB = new Knight(new Field(0,1), Color.BLACK);
        Figure blackKnightG = new Knight(new Field(0,6), Color.BLACK);
        Figure blackBishopC = new Bishop(new Field(0,2), Color.BLACK);
        Figure blackBishopF = new Bishop(new Field(0,5), Color.BLACK);
        Figure blackQueen = new Queen(new Field(0,3), Color.BLACK);
        Figure blackKing = new King(new Field(0,4), Color.BLACK);
        //Adding to figures collection
        register(blackPawnA);
        register(blackPawnB);
        register(blackPawnC);
        register(blackPawnD);
        register(blackPawnE);
        register(blackPawnF);
        register(blackPawnG);
        register(blackPawnH);
        register(blackRockA);
        register(blackRockH);
        register(blackKnightB);
        register(blackKnightG);
        register(blackBishopC);
        register(blackBishopF);
        register(blackQueen);
        register(blackKing);

        setTakenFields();
//        blackPawnA.possibleTurns();
        figures.forEach(v -> ((Figure)v).possibleTurns());
        whiteFigures.forEach(white -> fieldsUnderWhiteInfluence.addAll(((Figure)white).getFieldsUnderMyInfluence()));
        blackFigures.forEach(black -> fieldsUnderBlackInfluence.addAll(((Figure) black).getFieldsUnderMyInfluence()));
    }

    public List<Figure> getFiguresByClass(Class clazz){
        List<Figure> returnedFigures = new ArrayList<Figure>();
        figures.stream().filter(f -> f.getClass() == clazz).forEach(f -> returnedFigures.add((Figure) f));
        return returnedFigures;
    }

    public List<Figure> getFiguresByClass(Class clazz, Color color){
        List<Observer> observers = getFigures(color);
        List<Figure> figures = new ArrayList<>();
        observers.stream().filter(f -> f.getClass() == clazz).forEach(observer -> figures.add((Figure) observer));
        return figures;
    }

    public King getKing(Color color){
        return (King) getFigures(color).stream().filter(f -> f.getClass() == King.class).collect(Collectors.toList()).get(0);
    }

    public List<Observer> getFigures(Color color){
        return color == Color.WHITE ? whiteFigures : blackFigures;
    }

//    public Field getPreviousTurn() {
//        return previousTurn;
//    }
//
//    public void setPreviousTurn(Field previousTurn) {
//        this.previousTurn = previousTurn;
//    }

    private void updateFieldsUnderWhiteInfluence(){
        fieldsUnderWhiteInfluence.clear();
        whiteFigures.forEach(w -> fieldsUnderWhiteInfluence.addAll(((Figure)w).getFieldsUnderMyInfluence()));
    }

    private void updateFieldsUnderBlackInfluence(){
        fieldsUnderBlackInfluence.clear();
        blackFigures.forEach(b -> fieldsUnderBlackInfluence.addAll(((Figure)b).getFieldsUnderMyInfluence()));
    }

    public boolean isFieldValid(Field field){
        return field.getX() >= 0 && field.getX() < SIZE && field.getY() >= 0 && field.getY() < SIZE;
    }

    public List<Turn> getPossibleTurnsAndKillings() {
        return possibleTurnsAndKillings;
    }

    public List<Observer> getFigures() {
        return figures;
    }

    public Set<Field> getFieldsUnderWhiteInfluence() {
        return fieldsUnderWhiteInfluence;
    }

    public Set<Field> getFieldsUnderBlackInfluence() {
        return fieldsUnderBlackInfluence;
    }

    public Map<Field, Figure> getFieldToFigure(){
        return fieldToFigure;
    }

    public Turn getPreviousTurn() {
        return previousTurn;
    }

    public Turn getCurrentTurn(){
        return currentTurn;
    }

    public int getTurnNumber(){
        return turnNumber;
    }

    public void setCurrentTurn(Turn currentTurn) {
        this.previousTurn = this.currentTurn;
        this.currentTurn = currentTurn;
        turnNumber = currentTurn.getNumberOfTurn();
    }

    public void notify(Observer figure) {
        updateTakenFields(figure);
        figure.update(field);
        figures.forEach(cf -> {
            if (!cf.equals(figure)) {
                ((Figure) cf).update();
            }
        });
        updateFieldsUnderWhiteInfluence();
        updateFieldsUnderBlackInfluence();

    }

    public void register(Observer figure) {
        figures.add(figure);
        if (((Figure) figure).getColor() == Color.WHITE){
            whiteFigures.add(figure);
        } else {
            blackFigures.add(figure);
        }
        Field field = ((Figure) figure).getField();
        fieldToFigure.put(((Figure) figure).getField(), ((Figure)figure));
        takenFields.add(field);
    }

    public void removeFigure(Observer figure) {
        figures.remove(figure);
//        System.out.println("Figures after = " + figures);
        if (((Figure)figure).getColor() == Color.BLACK){
//            System.out.println(figure + " is black");
            blackFigures.remove(figure);
        } else {
//            System.out.println(figure + " is white");
            whiteFigures.remove(figure);
        }
        fieldToFigure.remove(((Figure) figure).getField());
        takenFields.remove(((Figure)figure).getField());
    }

    public void setNewCoordinates(Turn turn, Figure updatedFigure, Field updatedField, Figure eatenFigure, boolean isUndoing, boolean enPassant){
//        previousTurn = currentTurn;
//        currentTurn = turn;
        if (eatenFigure != null){
            removeFigure(eatenFigure);
        }
        if (figures.contains(updatedFigure)){

            this.field = updatedField;
            notify(updatedFigure);
            figures.forEach(f -> {
                    ((Figure)f).possibleTurns();
                    ((Figure)f).attackedFields();
            });
        }else {
            throw new RuntimeException("There is no such figure on the Board." + updatedFigure);
        }
        if (isUndoing){
            Figure figureToResurrect = ProcessingUtils.eatenFigureToResurrection;
            if (figureToResurrect != null){
//                Process.printAllBoard();
                register(figureToResurrect);
            }
        }
        if (enPassant){
            //TODO implement the logic for enPassant
        }
    }

    public Set<Field> getTakenFields() {
        return takenFields;
    }

    private void updateTakenFields(Observer figure){
        takenFields.remove(((Figure) figure).getField());
        takenFields.add(field);
        fieldToFigure.put(field, (Figure) figure);
        fieldToFigure.replace(((Figure) figure).getField(), null);
    }

    private void setTakenFields(){
        figures.forEach(f -> takenFields.add(((Figure)f).getField()));
    }
}