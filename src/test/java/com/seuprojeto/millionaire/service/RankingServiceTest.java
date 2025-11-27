package com.seuprojeto.millionaire.service;

import com.seuprojeto.millionaire.model.GameSession;
import com.seuprojeto.millionaire.model.Player;
import com.seuprojeto.millionaire.repository.GameSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {
    
    @Mock
    private GameSessionRepository gameSessionRepository;
    
    @InjectMocks
    private RankingService rankingService;
    
    private Player testPlayer;
    private GameSession testSession1;
    private GameSession testSession2;
    
    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
            .id(1L)
            .nickname("TestPlayer")
            .build();
        
        testSession1 = GameSession.builder()
            .id(1L)
            .player(testPlayer)
            .score(1_000_000)
            .status(GameSession.GameStatus.WON)
            .startedAt(LocalDateTime.now())
            .build();
        
        testSession2 = GameSession.builder()
            .id(2L)
            .player(testPlayer)
            .score(50_000)
            .status(GameSession.GameStatus.LOST)
            .startedAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    void shouldGetTop10Ranking() {
        List<GameSession> sessions = Arrays.asList(testSession1, testSession2);
        when(gameSessionRepository.findTop10ByStatusNotOrderByScoreDescStartedAtDesc(
            GameSession.GameStatus.IN_PROGRESS
        )).thenReturn(sessions);
        
        List<GameSession> result = rankingService.getTop10Ranking();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1_000_000, result.get(0).getScore());
        verify(gameSessionRepository, times(1))
            .findTop10ByStatusNotOrderByScoreDescStartedAtDesc(GameSession.GameStatus.IN_PROGRESS);
    }
    
    @Test
    void shouldReturnEmptyListWhenNoRankings() {
        when(gameSessionRepository.findTop10ByStatusNotOrderByScoreDescStartedAtDesc(
            GameSession.GameStatus.IN_PROGRESS
        )).thenReturn(Arrays.asList());
        
        List<GameSession> result = rankingService.getTop10Ranking();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

