package com.seuprojeto.millionaire.controller;

import com.seuprojeto.millionaire.model.GameSession;
import com.seuprojeto.millionaire.model.Player;
import com.seuprojeto.millionaire.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RankingControllerTest {
    
    private MockMvc mockMvc;
    private RankingService rankingService;
    private RankingController rankingController;
    
    private List<GameSession> testSessions;
    
    @BeforeEach
    void setUp() {
        rankingService = mock(RankingService.class);
        rankingController = new RankingController(rankingService);
        mockMvc = MockMvcBuilders.standaloneSetup(rankingController).build();
        
        Player player1 = Player.builder()
            .id(1L)
            .nickname("Player1")
            .build();
        
        Player player2 = Player.builder()
            .id(2L)
            .nickname("Player2")
            .build();
        
        GameSession session1 = GameSession.builder()
            .id(1L)
            .player(player1)
            .score(1_000_000)
            .status(GameSession.GameStatus.WON)
            .startedAt(LocalDateTime.now())
            .build();
        
        GameSession session2 = GameSession.builder()
            .id(2L)
            .player(player2)
            .score(50_000)
            .status(GameSession.GameStatus.LOST)
            .startedAt(LocalDateTime.now())
            .build();
        
        testSessions = Arrays.asList(session1, session2);
    }
    
    @Test
    void shouldDisplayRankingPage() throws Exception {
        when(rankingService.getTop10Ranking()).thenReturn(testSessions);
        
        Model model = mock(Model.class);
        String viewName = rankingController.ranking(model);
        
        assertEquals("ranking", viewName);
        verify(rankingService, times(1)).getTop10Ranking();
        verify(model, times(1)).addAttribute(eq("topGames"), eq(testSessions));
    }
    
    @Test
    void shouldHandleEmptyRanking() throws Exception {
        when(rankingService.getTop10Ranking()).thenReturn(Arrays.asList());
        
        Model model = mock(Model.class);
        String viewName = rankingController.ranking(model);
        
        assertEquals("ranking", viewName);
        verify(rankingService, times(1)).getTop10Ranking();
    }
}

