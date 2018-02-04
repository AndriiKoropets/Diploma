package com.koropets.diploma.chess.process.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Builder
public class FinalResult {

    private int first;
    private int second;
    private int third;
    private int fourth;
    private int fifth;
    private int sixth;
    private int seventh;
    private int eighth;
}
