package com.scentbird.scentbird_tic_tac_toe.handler;

import com.scentbird.scentbird_tic_tac_toe.model.MoveEvent;
import com.scentbird.scentbird_tic_tac_toe.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GameStompMessageHandler extends StompSessionHandlerAdapter {
    private static final String MOVE_TOPIC = "/app/game";
    private final GameService gameService;
    private StompSession session;
    private final WebSocketStompClient stompClient;

    private String hostUrl = "";

    public GameStompMessageHandler(GameService gameService, WebSocketStompClient stompClient) {
        this.gameService = gameService;
        this.stompClient = stompClient;
    }

    public void updateHostUrl(String url) {
        this.hostUrl = url;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        session.subscribe("/topic/moves", this);
        gameService.startNewGame();
        session.send(MOVE_TOPIC, gameService.makeMove());
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.info("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return MoveEvent.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        MoveEvent moveEvent = (MoveEvent) payload;
        gameService.initGameIfNeeded(moveEvent);
        gameService.checkWinnerAndStartAgain();
        session.send(MOVE_TOPIC, gameService.saveAndMakeMove(moveEvent));
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        if (!session.isConnected()) {
            reestablishConnection();
        } else {
            log.error(String.format("Connection is lost, the cause is :  %s , trying to reconnect.", exception.getMessage()));
        }
    }

    public void disconnect() {
        if (session != null && session.isConnected())
            session.disconnect();
    }

    private void reestablishConnection() {
        boolean disconnected = true;
        while (disconnected) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
            }
            try {
                stompClient.connect(hostUrl, this).get();
                disconnected = false;
            } catch (Exception e) {
                log.error("Couldn't restore connection. Error message : " + e.getMessage());
            }
        }
    }
}
