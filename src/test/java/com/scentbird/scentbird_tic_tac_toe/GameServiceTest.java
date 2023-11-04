package com.scentbird.scentbird_tic_tac_toe;

import com.scentbird.scentbird_tic_tac_toe.handler.GameStompMessageHandler;
import com.scentbird.scentbird_tic_tac_toe.model.Figure;
import com.scentbird.scentbird_tic_tac_toe.model.GameSession;
import com.scentbird.scentbird_tic_tac_toe.model.MoveEvent;
import com.scentbird.scentbird_tic_tac_toe.model.Player;
import com.scentbird.scentbird_tic_tac_toe.service.GameService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GameServiceTest {
    @Spy
    private GameService gameService = new GameService();

    @Mock
    private WebSocketClient socketClient;

    @Mock
    private WebSocketStompClient stompClient;

    @Mock
    private GameStompMessageHandler sessionHandler;

    @Mock
    private ListenableFuture<StompSession> stompSession;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        gameService.setSocketClient(socketClient);
        gameService.setStompClient(stompClient);
        gameService.setSessionHandler(sessionHandler);

        when(stompClient.connect(Mockito.anyString(), Mockito.any(GameStompMessageHandler.class))).thenReturn(stompSession);
//        gameService.initClient();
    }

    @Test
    public void testInitNewGame() {
        MoveEvent moveEvent = new MoveEvent();
        Player player = new Player();
        player.setFigure(Figure.X);
        moveEvent.setPlayer(player);

        gameService.initNewGame(moveEvent);

        assertEquals(Figure.O, gameService.getPlayerInfo().getFigure());
    }

    @Test
    public void testStartNewGameWithIPAndPort() {
        gameService.startNewGame("localhost", 8080);

        // Ensure that currentSession is not null
        assertTrue(gameService.getCurrentSession() != null);

        // Verify that disconnect and connect methods are called
        verify(sessionHandler, times(1)).disconnect();
        verify(sessionHandler, times(1)).updateHostUrl(Mockito.anyString());
    }

    @Test
    public void testStartNewGame() {
        gameService.startNewGame();

        // Ensure that currentSession is not null
        assertTrue(gameService.getCurrentSession() != null);
    }

    @Test
    public void testIsGameEnded() {
        GameSession gameSession = new GameSession();
        gameSession.setEnded(true);
        gameService.setCurrentSession(gameSession);

        assertTrue(gameService.isGameEnded());
    }

    @Test
    public void testSaveAndMakeMove() {
        MoveEvent moveEvent = new MoveEvent();
        Player player = new Player();
        player.setFigure(Figure.X);
        moveEvent.setPlayer(player);
        moveEvent.setX(1);
        moveEvent.setY(1);

        gameService.startNewGame();
        MoveEvent result = gameService.saveAndMakeMove(moveEvent);

        assertNotNull(result);
    }

    @Test
    public void testMakeMove() {
        gameService.startNewGame();
        MoveEvent result = gameService.makeMove();

        assertNotNull(result);
    }

    @Test
    public void testCheckWinnerAndStartAgain() {
        // Create a game session with a winner
        GameSession gameSession = new GameSession();
        Integer[][] board = {
                {1, null, null},
                {null, 1, null},
                {null, null, 1}
        };
        gameSession.setBoard(board);
        gameSession.setEnded(true);
        gameService.setCurrentSession(gameSession);

        assertTrue(gameService.checkWinnerAndStartAgain());
        assertTrue(gameService.getCurrentSession() != null);

        // Create a game session without a winner
        gameSession.setBoard(new Integer[3][3]);
        gameSession.setEnded(false);
        gameService.setCurrentSession(gameSession);

        assertFalse(gameService.checkWinnerAndStartAgain());
        assertTrue(gameService.getCurrentSession() != null);
    }

    @Test
    public void testCheckWinner() {
        // Test for winning in a row
        GameSession gameSession = new GameSession();
        Integer[][] board = {
                {1, 1, 1},
                {null, null, null},
                {null, null, null}
        };
        gameSession.setBoard(board);
        gameService.setCurrentSession(gameSession);

        Integer winner = gameService.checkWinner();
        assertEquals(Integer.valueOf(1), winner);

        // Test for winning in a column
        board = new Integer[][] {
                {1, null, null},
                {1, null, null},
                {1, null, null}
        };
        gameSession.setBoard(board);
        gameService.setCurrentSession(gameSession);

        winner = gameService.checkWinner();
        assertEquals(Integer.valueOf(1), winner);

        // Test for winning in a diagonal
        board = new Integer[][] {
                {1, null, null},
                {null, 1, null},
                {null, null, 1}
        };
        gameSession.setBoard(board);
        gameService.setCurrentSession(gameSession);

        winner = gameService.checkWinner();
        assertEquals(Integer.valueOf(1), winner);

        // Test for no winner
        board = new Integer[][] {
                {1, 0, 1},
                {null, 0, null},
                {null, 1, null}
        };
        gameSession.setBoard(board);
        gameService.setCurrentSession(gameSession);

        winner = gameService.checkWinner();
        assertNull(winner);
    }

    @Test
    public void testCheckField() {
        Integer[][] board = {
                {1, null, 1},
                {null, 0, null},
                {null, 1, null}
        };
        MoveEvent moveEvent = new MoveEvent();
        moveEvent.setGameId("gameId");
        moveEvent.setX(1);
        moveEvent.setY(0);
        moveEvent.setPlayer(new Player());
        moveEvent.getPlayer().setFigure(Figure.X);

        gameService.startNewGame();
        GameSession currentSession = gameService.getCurrentSession();
        currentSession.setBoard(board);

        boolean result = gameService.checkField(moveEvent, board, 1);
        assertTrue(result);
    }

    @Test
    public void testUpdateSession() {
        MoveEvent moveEvent = new MoveEvent();
        moveEvent.setX(0);
        moveEvent.setY(1);
        Player player = new Player();
        player.setFigure(Figure.X);
        moveEvent.setPlayer(player);

        gameService.startNewGame();
        GameSession currentSession = gameService.getCurrentSession();
        moveEvent.setGameId(currentSession.getGameId());

        gameService.updateSession(moveEvent);

        assertEquals(1, (int) currentSession.getBoard()[1][0]);
    }

    @Test
    public void testInitGameIfNeeded() {
        // Test when currentSession is null
        Player player = new Player();
        player.setFigure(Figure.X);
        MoveEvent moveEvent = new MoveEvent();
        moveEvent.setPlayer(player);
        moveEvent.setGameId("gameId");

        gameService.initGameIfNeeded(moveEvent);

        // Ensure that a new game session is created
        assertTrue(gameService.getCurrentSession() != null);

        // Test when currentSession has a different gameId
        GameSession gameSession = new GameSession();
        gameSession.setGameId("differentGameId");
        gameService.setCurrentSession(gameSession);

        gameService.initGameIfNeeded(moveEvent);

        // Ensure that a new game session is created
        assertTrue(gameService.getCurrentSession() != null);

        // Test when currentSession has the same gameId
        gameSession.setGameId("gameId");
        gameService.setCurrentSession(gameSession);

        gameService.initGameIfNeeded(moveEvent);

        // Ensure that the current session is not changed
        assertEquals(gameSession, gameService.getCurrentSession());
    }
}

