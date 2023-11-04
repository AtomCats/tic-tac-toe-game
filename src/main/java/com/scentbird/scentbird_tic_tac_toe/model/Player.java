package com.scentbird.scentbird_tic_tac_toe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Player {
    String playerId;
    Figure figure;
}
