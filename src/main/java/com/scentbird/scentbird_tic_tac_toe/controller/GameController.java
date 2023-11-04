package com.scentbird.scentbird_tic_tac_toe.controller;

import com.scentbird.scentbird_tic_tac_toe.model.GameSession;
import com.scentbird.scentbird_tic_tac_toe.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    GameService gameService;

    @GetMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    public void start(@Validated @RequestParam String ip, @Validated @RequestParam int port) {
        gameService.startNewGame(ip, port);
    }

    @GetMapping("/state")
    @ResponseStatus(HttpStatus.OK)
    public GameSession getGameState() {
        return gameService.getGameState();
    }
}
