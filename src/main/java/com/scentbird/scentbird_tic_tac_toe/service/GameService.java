package com.scentbird.scentbird_tic_tac_toe.service;

import com.scentbird.scentbird_tic_tac_toe.handler.GameStompMessageHandler;
import com.scentbird.scentbird_tic_tac_toe.model.Figure;
import com.scentbird.scentbird_tic_tac_toe.model.GameSession;
import com.scentbird.scentbird_tic_tac_toe.model.MoveEvent;
import com.scentbird.scentbird_tic_tac_toe.model.Player;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Random;
import java.util.Stack;
import java.util.UUID;

@Service
@Slf4j
@Setter
@Getter
public class GameService {
    private static final Stack<MoveEvent> EVENTS = new Stack<>();
    private static final String WS_URL_PATTERN = "ws://%s:%s/game";

    WebSocketClient socketClient;
    WebSocketStompClient stompClient;
    GameStompMessageHandler sessionHandler;
    private final String playerId = UUID.randomUUID().toString();
    private GameSession currentSession;
    private Player playerInfo;

    /**
     * Initializes the WebSocket client for the game service.
     */
    @PostConstruct
    public void initClient() {
        socketClient = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(socketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        sessionHandler = new GameStompMessageHandler(this, stompClient);
    }

    /**
     * Initializes the player with a random figure (X or O).
     */
    private void initPlayer() {
        playerInfo = new Player(playerId, Figure.valueOf(new Random(2).nextInt()));
    }

    /**
     * Initializes the player with the figure which is opposite to opponent's figure.
     *
     * @param opponentFigure The figure of the opponent.
     */
    private void initPlayer(Figure opponentFigure) {
        playerInfo = new Player(playerId, Figure.getOppositeTo(opponentFigure));
    }

    /**
     * Initializes a new game session with a random game ID.
     */
    private void initSession() {
        currentSession = GameSession.builder()
                .gameId(UUID.randomUUID().toString())
                .player1(playerInfo)
                .build();
    }

    /**
     * Initializes a new game session with an id from external move event.
     *
     * @param externalMove The external move event that triggered a new game session.
     */
    private void initSession(MoveEvent externalMove) {
        currentSession = GameSession.builder()
                .gameId(externalMove.getGameId())
                .player1(playerInfo)
                .player2(externalMove.getPlayer())
                .build();
    }

    /**
     * Initializes a new game based on an external move event, starting with the player's figure.
     *
     * @param externalMove The external move event that triggered a new game.
     */
    public void initNewGame(MoveEvent externalMove) {
        log.info("Starting new game");
        initPlayer(externalMove.getPlayer().getFigure());
        initSession(externalMove);
        EVENTS.clear();
    }

    /**
     * Starts a new game with the provided IP and port.
     *
     * @param ip   The IP address of the game server.
     * @param port The port of the game server.
     */
    public void startNewGame(String ip, int port) {
        log.info("Starting new game");
        String hostUrl = String.format(WS_URL_PATTERN, ip, port);
        initPlayer();
        initSession();
        EVENTS.clear();
        sessionHandler.disconnect();
        sessionHandler.updateHostUrl(hostUrl);
        stompClient.connect(hostUrl, sessionHandler);
    }

    /**
     * Starts a new game with default settings (random player figure and game session).
     */
    public void startNewGame() {
        log.info("Starting new game");
        initPlayer();
        initSession();
        EVENTS.clear();
    }

    /**
     * Checks whether the current game session has ended.
     *
     * @return True if the game session has ended, false otherwise.
     */
    public boolean isGameEnded() {
        return currentSession.isEnded();
    }

    /**
     * Saves the move event, makes a move, and sends a message back to the server.
     *
     * @param moveEvent The move event to save and respond to.
     * @return The next move event.
     */
    public MoveEvent saveAndMakeMove(MoveEvent moveEvent) {
        log.debug("Making move and sending message back");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info(String.format("Player with figure %s made new move to x = %s y = %s ", moveEvent.getPlayer().getFigure().name(),
                moveEvent.getX(), moveEvent.getY()));
        EVENTS.push(moveEvent);
        updateSession(moveEvent);

        return makeMove();
    }

    /**
     * Makes a move and returns the next move event.
     *
     * @return The next move event.
     */
    public MoveEvent makeMove() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        MoveEvent nextMove = getNextMove();
        log.info(String.format("Player with figure %s made new move to x = %s y = %s ", playerInfo.getFigure().name(),
                nextMove.getX(), nextMove.getY()));
        EVENTS.push(nextMove);
        return nextMove;
    }

    /**
     * Retrieves the current game state (session) information.
     *
     * @return The current game session.
     */
    public GameSession getGameState() {
        return currentSession;
    }

    /**
     * Checks for a winner in the current game session and starts a new game if there's a winner.
     *
     * @return True if the game ended and a new game has started, false otherwise.
     */
    public boolean checkWinnerAndStartAgain() {
        Integer winner = checkWinner();
        boolean gameEnded = false;

        if (winner != null) {
            log.info(String.format("Player with figure %s is won !", Figure.valueOf(winner).name()));
            startNewGame();
            gameEnded = true;
        }
        return gameEnded;
    }

    /**
     * Checks for a winner in the current game session.
     *
     * @return The figure of the winner (1 for X, 0 for O), or null if there's no winner yet.
     */
    public Integer checkWinner() {
        Integer[][] board = currentSession.getBoard();
        Integer result = null;

        // Check rows
        for (int row = 0; row < 3; row++) {
            if (board[row][0] != null && board[row][0] == board[row][1] && board[row][0] == board[row][2]) {
                return board[row][0];
            }
        }

        // Check columns
        for (int col = 0; col < 3; col++) {
            if (board[0][col] != null && board[0][col] == board[1][col] && board[0][col] == board[2][col]) {
                return board[0][col];
            }
        }

        // Check diagonals
        if (board[0][0] != null && board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
            return board[0][0];
        }

        if (board[0][2] != null && board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
            return board[0][2];
        }

        return result;  // If no winner yet, return null to indicate the game is ongoing or a tie.
    }

    /**
     * Gets the next move event, either a winning move or a blocking move, or a random move if none found.
     *
     * @return The next move event.
     */
    private MoveEvent getNextMove() {
        Integer[][] board = currentSession.getBoard();
        int figureValue = playerInfo.getFigure().value();
        MoveEvent nextMove = MoveEvent.builder()
                .gameId(currentSession.getGameId())
                .player(playerInfo)
                .build();

        // Check for a winning move
        if (checkField(nextMove, board, figureValue)) return nextMove;

        // Check for a blocking move (preventing the opponent from winning)
        int opponentSymbolValue = (figureValue == 1) ? 1 : 0;
        if (checkField(nextMove, board, opponentSymbolValue)) return nextMove;

        // If no winning or blocking moves, make a random move
        while (true) {
            int row = (int) (Math.random() * 3);
            int col = (int) (Math.random() * 3);
            if (board[row][col] == null) {
                nextMove.setY(row);
                nextMove.setX(col);
                return nextMove;
            }
        }

    }


    /**
     * Checks if a specific move is a winning move for the given player figure.
     *
     * @param nextMove            The move event to check.
     * @param board               The game board.
     * @param symbolValue The opponent's figure value (1 for X, 0 for O).
     * @return True if the move is a winning move, false otherwise.
     */
    public boolean checkField(MoveEvent nextMove, Integer[][] board, int symbolValue) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == null) {
                    // Simulate a move and check if it leads to a win
                    board[row][col] = symbolValue;
                    if (checkWinner() != null && checkWinner() == symbolValue) {
                        nextMove.setY(row);
                        nextMove.setX(col);
                        return true;
                    }
                    // Undo the move
                    board[row][col] = null;
                }
            }
        }
        return false;
    }

    /**
     * Updates the current game session with a new move event.
     *
     * @param moveEvent The move event to update the game session.
     */
    public void updateSession(MoveEvent moveEvent) {
        if (currentSession.getGameId().equals(moveEvent.getGameId())) {
            Integer[][] board = currentSession.getBoard();
            board[moveEvent.getY()][moveEvent.getX()] = moveEvent.getPlayer().getFigure().value();
        }
    }

    /**
     * Initializes a new game session if needed based on an external move event.
     *
     * @param moveEvent The external move event.
     */
    public void initGameIfNeeded(MoveEvent moveEvent) {
        if (currentSession == null || !currentSession.getGameId().equals(moveEvent.getGameId()))
            initNewGame(moveEvent);
    }
}
