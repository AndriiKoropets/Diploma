package com.koropets.diploma.chess.model;

import java.util.Set;

import static java.lang.Math.abs;
import static com.koropets.diploma.chess.process.constants.Constants.SIZE;

public class King extends Figure {

    private boolean opportunityToCastling = true;
    private static final int KING_WEIGHT = Integer.MAX_VALUE;
    private static final int POINT = 5;

//    @Autowired
//    private Board board;

    public King(Field field, Color color) {
        super(field, color);
        attackedFields();
    }

    @Override
    public void possibleTurns(){
        Set<Field> enemyInfluence = (this.getColor() == Color.BLACK) ? board.getFieldsUnderWhiteInfluence()
                : board.getFieldsUnderBlackInfluence();
        this.getAttackedFields().forEach(f -> {
            Figure figure = board.getFieldToFigure().get(f);
            if (!enemyInfluence.contains(f)){
                if (figure != null){
                    if (this.getColor() == figure.getColor()){
                        figure.addAllyProtectMe(this);
                        this.addAllyIProtect(figure);
                    }else {
                        figure.addEnemy(this);
                        this.getWhoCouldBeEaten().add(figure);
                        this.getPreyField().add(figure.getField());
                    }
                }else {
                    this.getPossibleFieldsToMove().add(f);
                    this.getFieldsUnderMyInfluence().add(f);
                }
            }
        });
    }

    @Override
    public double getValue() {
        return KING_WEIGHT;
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
        return new King(this.getField(), this.getColor());
    }

    public boolean isOpportunityToCastling() {
        return opportunityToCastling;
    }

    public void looseOpportunityToCastling() {
        this.opportunityToCastling = false;
    }

    public boolean isUnderAttack(){
        Set<Field> enemyInfluence = (this.getColor() == Color.WHITE) ? board.getFieldsUnderBlackInfluence()
                : board.getFieldsUnderWhiteInfluence();
        return enemyInfluence.contains(this.getField());
    }

    @Override
    protected void attackedFields() {
        for (int  i = 0; i < SIZE; i++){
            for (int j = 0; j < SIZE; j++){
                if ((abs(this.getField().getX() - i) <= 1) && (abs(this.getField().getY() - j) <= 1)) {
                    if (this.getField().getX() == i && this.getField().getY() == j){
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
        return "K" + this.getField().toString();
    }
}