package com.scentbird.scentbird_tic_tac_toe.controller;

import com.scentbird.scentbird_tic_tac_toe.model.MoveEvent;
import com.scentbird.scentbird_tic_tac_toe.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class WebSocketGameController {
    @Autowired
    GameService gameService;


    @MessageMapping("/game")
    @SendTo("/topic/moves")
    public MoveEvent makeMove(MoveEvent moveEvent) {
        log.debug("Received message : " + moveEvent);
        gameService.initGameIfNeeded(moveEvent);
        gameService.checkWinnerAndStartAgain();
        return gameService.saveAndMakeMove(moveEvent);
    }

}
