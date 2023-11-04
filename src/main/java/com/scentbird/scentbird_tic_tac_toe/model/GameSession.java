package com.scentbird.scentbird_tic_tac_toe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {
    String gameId;
    boolean isEnded = false;
    Player player1;
    Player player2;
    @Builder.Default
    Integer[][] board = new Integer[3][3];
}
