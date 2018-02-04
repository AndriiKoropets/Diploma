package com.koropets.diploma.chess.model;

import com.koropets.diploma.chess.process.constants.Constants;

import static java.lang.Math.abs;

import java.util.HashSet;
import java.util.Set;

/**
 * @author AndriiKoropets
 */
public class Bishop extends Figure {

    private final static int BISHOP_WEIGHT = 3;
    private final static int POINT = 2;

    public Bishop(Field field, Color color) {
        super(field, color);
        attackedFields();
    }

    @Override
    protected void attackedFields() {
        for (int i = 0; i < Constants.SIZE; i++){
            for (int j = 0; j < Constants.SIZE; j++){
                if (abs(this.getField().getX() - i) == abs(this.getField().getY() - j) && abs(this.getField().getY() - j) != 0){
                    this.getAttackedFields().add(new Field(i, j));
                }
            }
        }
    }

    @Override
    public void possibleTurns() {
        for (int i = this.getField().getX() + 1; i < Constants.SIZE; i++){
            boolean flag = false;
            for (int j = this.getField().getY() + 1; j < Constants.SIZE; j++){
                if (i < Constants.SIZE && j < Constants.SIZE &&  abs(this.getField().getX() - i) == abs(this.getField().getY() - j)){
                    Field field = new Field(i, j);
                    if (checkingFieldForTaken(field)){
                        flag = true;
                        break;
                    }else {
                        this.getFieldsUnderMyInfluence().add(field);
                    }
                }
            }
            if (flag){
                break;
            }
        }
        for (int i = this.getField().getX() + 1; i < Constants.SIZE; i++){
            boolean flag = false;
            for (int j = this.getField().getY() - 1; j >= 0; j--){
                if (i < Constants.SIZE && j >= 0 && abs(this.getField().getX() - i) == abs(this.getField().getY() - j)){
                    Field field = new Field(i,j);
                    if (checkingFieldForTaken(field)){
                        flag = true;
                        break;
                    }else {
                        this.getFieldsUnderMyInfluence().add(field);
                    }
                }
            }
            if (flag){
                break;
            }
        }
        for (int i = this.getField().getX() - 1; i >= 0; i--){
            boolean flag = false;
            for (int j = this.getField().getY() + 1; j < Constants.SIZE; j++){
                if (i >= 0 && abs(this.getField().getX() - i) == abs(this.getField().getY() - j)){
                    Field field = new Field(i,j);
                    if (checkingFieldForTaken(field)){
                        flag = true;
                        break;
                    }else {
                        this.getFieldsUnderMyInfluence().add(field);
                    }
                }
            }
            if (flag){
                break;
            }
        }
        for (int i = this.getField().getX() - 1; i >= 0; i--){
            boolean flag = false;
            for (int j = this.getField().getY() - 1; j >= 0; j--){
                if (i >= 0 && j >= 0 && abs(this.getField().getX() - i) == abs(this.getField().getY() - j)){
                    Field field = new Field(i,j);
                    if (checkingFieldForTaken(field)){
                        flag = true;
                        break;
                    }else {
                        this.getFieldsUnderMyInfluence().add(field);
                    }
                }
            }
            if (flag){
                break;
            }
        }
    }

    @Override
    public double getValue() {
        return BISHOP_WEIGHT;
    }

    @Override
    public int getPoint() {
        return POINT;
    }

    @Override
    public Set<Figure> pullAdditionalAlliesAndEnemies() {
        Set<Figure> chosenAllies = new HashSet<>();
        this.getAlliesIProtect().forEach(f -> {
            if (f.getClass() == Bishop.class || f.getClass() == Queen.class){
                chosenAllies.add(f);
            }
        });
        return chosenAllies;
    }

    @Override
    public Figure createNewFigure() {
        return new Bishop(this.getField(), this.getColor());
    }

    @Override
    public String toString() {
        return "B" + this.getField().toString();
    }
}