package com.scentbird.scentbird_tic_tac_toe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveEvent {
    String gameId;
    Player player;
    Integer x;
    Integer y;
}
