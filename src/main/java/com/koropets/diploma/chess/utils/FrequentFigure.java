package com.koropets.diploma.chess.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class FrequentFigure {

    private int king;
    private int queen;
    private int bishop;
    private int knight;
    private int rock;
    private int pawn;

    void updateKing(){
        this.king++;
    }

    void updateQueen(){
        this.queen++;
    }

    void updateBishop(){
        this.bishop++;
    }

    void updateKnight(){
        this.knight++;
    }

    void updateRock(){
        this.rock++;
    }

    void updatePawn(){
        this.pawn++;
    }

}
