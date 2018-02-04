package com.koropets.diploma.chess.model;

import static java.lang.Math.abs;
import static com.koropets.diploma.chess.process.constants.Constants.SIZE;

import java.util.Set;
/**
 * @author AndriiKoropets
 */
public class Knight extends Figure {

    private final static int KNIGHT_WEIGHT = 3;
    private final static int POINT = 2;

    public Knight(Field field, Color color) {
        super(field, color);
        attackedFields();
    }

    @Override
    public void possibleTurns(){
        for (Field field : getAttackedFields()){
            if(!checkingFieldForTaken(field)){
                this.getFieldsUnderMyInfluence().add(field);
            }
        }
    }

    @Override
    public double getValue() {
        return KNIGHT_WEIGHT;
    }

    @Override
    public int getPoint() {
        return POINT;
    }

    @Override
    public Set<Figure> pullAdditionalAlliesAndEnemies() {
        return null;
    }

    @Override
    public Figure createNewFigure() {
        return new Knight(this.getField(), this.getColor());
    }

    @Override
    protected void attackedFields() {
        for (int i = 0; i < SIZE; i++){
            for (int j = 0; j < SIZE; j++){
                if (abs(this.getField().getX() - i) + abs(this.getField().getY() - j) == 3){
                    if (this.getField().getX()== i || this.getField().getY() == j){
                        continue;
                    }
                    Field field = new Field(i, j);
                    this.getAttackedFields().add(field);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "N" + this.getField().toString();
    }
}
