package com.seuprojeto.millionaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {
    
    private GameSession gameSession;
    private Player player;
    
    @BeforeEach
    void setUp() {
        player = Player.builder()
            .id(1L)
            .nickname("TestPlayer")
            .build();
        
        gameSession = GameSession.builder()
            .id(1L)
            .player(player)
            .currentLevel(1)
            .score(0)
            .skipsLeft(3)
            .fiftyFiftyUsed(false)
            .aiHelpUsed(false)
            .status(GameSession.GameStatus.IN_PROGRESS)
            .build();
    }
    
    @Test
    void shouldIncrementLevel() {
        int initialLevel = gameSession.getCurrentLevel();
        gameSession.incrementLevel();
        
        assertEquals(initialLevel + 1, gameSession.getCurrentLevel());
    }
    
    @Test
    void shouldDecrementSkipsWhenAvailable() {
        int initialSkips = gameSession.getSkipsLeft();
        gameSession.useSkip();
        
        assertEquals(initialSkips - 1, gameSession.getSkipsLeft());
    }
    
    @Test
    void shouldNotDecrementSkipsWhenNoneLeft() {
        gameSession.setSkipsLeft(0);
        gameSession.useSkip();
        
        assertEquals(0, gameSession.getSkipsLeft());
    }
    
    @Test
    void shouldMarkFiftyFiftyAsUsed() {
        assertFalse(gameSession.isFiftyFiftyUsed());
        gameSession.useFiftyFifty();
        assertTrue(gameSession.isFiftyFiftyUsed());
    }
    
    @Test
    void shouldMarkAiHelpAsUsed() {
        assertFalse(gameSession.isAiHelpUsed());
        gameSession.useAiHelp();
        assertTrue(gameSession.isAiHelpUsed());
    }
    
    @Test
    void shouldFinishGameWithStatus() {
        gameSession.finishGame(GameSession.GameStatus.WON);
        
        assertEquals(GameSession.GameStatus.WON, gameSession.getStatus());
        assertNotNull(gameSession.getFinishedAt());
    }
    
    @Test
    void shouldHaveInitialStatusAsInProgress() {
        GameSession newSession = GameSession.builder()
            .player(player)
            .build();
        
        assertEquals(GameSession.GameStatus.IN_PROGRESS, newSession.getStatus());
        assertEquals(1, newSession.getCurrentLevel());
        assertEquals(3, newSession.getSkipsLeft());
    }
}

