package com.koropets.diploma.chess.process.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.Builder;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AntiParameter {

    private int fifthParam;
    private int sixthParam;
    private int seventhParam;
    private int eighthParam;

}
