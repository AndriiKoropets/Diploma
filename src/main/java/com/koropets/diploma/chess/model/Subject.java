package com.koropets.diploma.chess.model;

/**
 * @author AndriiKoropets
 */
public interface Subject {

    void notify(Observer figure);
    void register(Observer figure);
    void removeFigure(Observer figure);
}