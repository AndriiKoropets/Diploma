package com.koropets.diploma.chess.process.dto;

import com.koropets.diploma.chess.model.Field;
import com.koropets.diploma.chess.model.Figure;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.Builder;
import scala.Tuple2;

import java.util.List;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Parameter {

    private int firstAttackEnemy;
    private int secondBeUnderAttack;
    private int thirdWithdrawAttackOnEnemy;
    private int fourthWithdrawAttackOnMe;
    private Tuple2<Integer, List<Tuple2<Figure, Field>>> fifthDontTakeAChanceToAttack;
    private Tuple2<Integer, List<Tuple2<Figure, Field>>> sixthDontTakeAChanceToBeUnderAttack;
    private Tuple2<Integer, List<Tuple2<Figure, Field>>> seventhDontTakeAChanceToWithdrawAttackOnEnemy;
    private Tuple2<Integer, List<Tuple2<Figure, Field>>> eighthDontTakeAChanceToWithdrawAttackOnMe;
}