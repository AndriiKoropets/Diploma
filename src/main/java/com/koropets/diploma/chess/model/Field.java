package com.koropets.diploma.chess.model;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.koropets.diploma.chess.process.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author AndriiKoropets
 */
@Slf4j
public class Field {

    private int x;
    private int y;
    private static final Map<Integer, Character> horizontal = new LinkedHashMap<Integer, Character>();
    private static final Map<Integer, Integer> vertical = new LinkedHashMap<Integer, Integer>();
    private static final Map<Character, Integer> invertedHorizontal = new LinkedHashMap<Character, Integer>();
    private static final Map<Integer, Integer> invertedVertical = new LinkedHashMap<Integer, Integer>();

    @Autowired
    private Board board;

    static {
        invertedVertical.put(8, 0);
        invertedVertical.put(7, 1);
        invertedVertical.put(6, 2);
        invertedVertical.put(5, 3);
        invertedVertical.put(4, 4);
        invertedVertical.put(3, 5);
        invertedVertical.put(2, 6);
        invertedVertical.put(1, 7);
    }

    static {
        invertedHorizontal.put('a', 0);
        invertedHorizontal.put('b', 1);
        invertedHorizontal.put('c', 2);
        invertedHorizontal.put('d', 3);
        invertedHorizontal.put('e', 4);
        invertedHorizontal.put('f', 5);
        invertedHorizontal.put('g', 6);
        invertedHorizontal.put('h', 7);
    }

    static {
        horizontal.put(0, 'a');
        horizontal.put(1, 'b');
        horizontal.put(2, 'c');
        horizontal.put(3, 'd');
        horizontal.put(4, 'e');
        horizontal.put(5, 'f');
        horizontal.put(6, 'g');
        horizontal.put(7, 'h');
    }

    static {
        vertical.put(0, 8);
        vertical.put(1, 7);
        vertical.put(2, 6);
        vertical.put(3, 5);
        vertical.put(4, 4);
        vertical.put(5, 3);
        vertical.put(6, 2);
        vertical.put(7, 1);
    }

    public Field(int x, int y){
        if (isValidField(x, y)){
            this.x = x;
            this.y = y;
            log.trace("Created field with such points: x = {}, y = {}", x, y);
        }else {
            log.error("Failed to created a field due to invalid points, x = {}, y = {}", x, y);
            throw new RuntimeException("Invalid points for field, x = " + x + ", y = " + y);
        }
    }

    public static boolean isValidField(int x, int y){
        return x >= 0 && x < Constants.SIZE && y >= 0 && y < Constants.SIZE;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static Map<Integer, Character> getHorizontal() {
        return horizontal;
    }

    public static Map<Integer, Integer> getVertical() {
        return vertical;
    }

    public static Map<Character, Integer> getInvertedHorizontal() {
        return invertedHorizontal;
    }

    public static Map<Integer, Integer> getInvertedVertical() {
        return invertedVertical;
    }

    public boolean isTaken(){
        return board.getTakenFields().contains(this);
    }

    //TODO refactor this method. Should be placed in Board class.
    public boolean isUnderInfluence(Color color){
        List<Observer> figures = board.getFigures(color);
        for (Object figure : figures){
            for (Object field : ((Figure)figure).getAttackedFields()){
                if (this.equals(field)){
                    return true;
                }
            }
        }
        return false;
    }

    public int distance(Field comparedField){
        return Math.abs(this.getX() - comparedField.getX()) + Math.abs(this.getY() - comparedField.getY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return this.getX() == field.getX() && this.getY() == field.getY();
    }

    @Override
    public int hashCode() {
        int result = this.getX();
        result = 31 * result + this.getY();
        return result;
    }

    @Override
    public String toString() {
        return horizontal.get(this.getY()) + "" + vertical.get(this.getX());
    }
}