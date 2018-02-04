package com.koropets.diploma.chess.process.dto;

import com.koropets.diploma.chess.model.Field;
import com.koropets.diploma.chess.model.Figure;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.AccessLevel;
import lombok.Builder;
import scala.Tuple2;

import java.util.List;

@Builder
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Turn {

    private List<Tuple2<Figure, Field>> figureToDestinationField;
    private Figure figureFromTransformation;
    private boolean eating;
    private boolean transformation;
    private Figure targetedFigure;
    private String writtenStyle;
    private int numberOfTurn;
    private boolean enPassant;
}