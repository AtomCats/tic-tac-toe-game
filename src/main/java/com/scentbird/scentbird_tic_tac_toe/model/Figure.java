package com.scentbird.scentbird_tic_tac_toe.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public enum Figure {
    X(1), O(0);

    public Integer value;

    public static Figure valueOf(int num) {
        return num == 0 ? Figure.O : Figure.X;
    }

    public static Figure getOppositeTo(Figure opponentFigure) {
        return switch (opponentFigure) {
            case O -> X;
            case X -> O;
        };
    }

    public int value() {
        return this.value;
    }
}
