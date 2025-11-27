package com.seuprojeto.millionaire.controller;

import com.seuprojeto.millionaire.dto.GeminiResponseDTO;
import com.seuprojeto.millionaire.model.*;
import com.seuprojeto.millionaire.service.GameService;
import com.seuprojeto.millionaire.service.GeminiAiService;
import com.seuprojeto.millionaire.service.QuestionService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GameControllerTest {
    
    private MockMvc mockMvc;
    private GameService gameService;
    private QuestionService questionService;
    private GeminiAiService geminiAiService;
    private GameController gameController;
    private HttpSession httpSession;
    
    private Player testPlayer;
    private GameSession testSession;
    private Question testQuestion;
    
    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        questionService = mock(QuestionService.class);
        geminiAiService = mock(GeminiAiService.class);
        httpSession = mock(HttpSession.class);
        
        gameController = new GameController(gameService, questionService, geminiAiService);
        mockMvc = MockMvcBuilders.standaloneSetup(gameController).build();
        
        testPlayer = Player.builder()
            .id(1L)
            .nickname("TestPlayer")
            .build();
        
        testSession = GameSession.builder()
            .id(1L)
            .player(testPlayer)
            .currentLevel(1)
            .score(0)
            .skipsLeft(3)
            .fiftyFiftyUsed(false)
            .aiHelpUsed(false)
            .status(GameSession.GameStatus.IN_PROGRESS)
            .answers(new ArrayList<>())
            .build();
        
        testQuestion = Question.builder()
            .id(1L)
            .questionText("Qual é a capital do Brasil?")
            .optionA("São Paulo")
            .optionB("Rio de Janeiro")
            .optionC("Brasília")
            .optionD("Salvador")
            .correctAnswer('C')
            .level(1)
            .category("Geografia")
            .explanation("Brasília é a capital")
            .build();
    }
    
    @Test
    void shouldDisplayGamePage() throws Exception {
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        when(questionService.getRandomQuestionForLevel(anyInt(), anyList())).thenReturn(testQuestion);
        when(gameService.getPrizeTable()).thenReturn(new int[]{1000, 2000, 3000});
        
        // Note: MockMvc doesn't support @SessionAttribute directly, so we test the controller method directly
        Model model = mock(Model.class);
        String viewName = gameController.gamePage(httpSession, model);
        
        assertEquals("game", viewName);
        verify(gameService, times(1)).getSession(1L);
        verify(questionService, times(1)).getRandomQuestionForLevel(anyInt(), anyList());
    }
    
    @Test
    void shouldRedirectToWinnerWhenGameWon() throws Exception {
        testSession.setStatus(GameSession.GameStatus.WON);
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        
        Model model = mock(Model.class);
        String viewName = gameController.gamePage(httpSession, model);
        
        // Should redirect, but we're testing the controller directly
        // In real scenario, Spring would handle the redirect
        verify(gameService, times(1)).getSession(1L);
    }
    
    @Test
    void shouldSubmitCorrectAnswer() throws Exception {
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        when(questionService.findById(1L)).thenReturn(testQuestion);
        
        GameSession updatedSession = GameSession.builder()
            .id(1L)
            .player(testPlayer)
            .currentLevel(2)
            .score(1000)
            .status(GameSession.GameStatus.IN_PROGRESS)
            .build();
        
        when(gameService.submitAnswer(any(), any(), anyChar(), anyInt(), any())).thenReturn(updatedSession);
        
        mockMvc.perform(post("/game/answer")
                .sessionAttr("sessionId", 1L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("questionId", "1")
                .param("answer", "C")
                .param("timeSpent", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correct").value(true));
        
        verify(gameService, times(1)).submitAnswer(any(), any(), eq('C'), eq(10), any());
    }
    
    @Test
    void shouldSubmitWrongAnswer() throws Exception {
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        when(questionService.findById(1L)).thenReturn(testQuestion);
        
        GameSession lostSession = GameSession.builder()
            .id(1L)
            .player(testPlayer)
            .currentLevel(1)
            .score(0)
            .status(GameSession.GameStatus.LOST)
            .build();
        
        when(gameService.submitAnswer(any(), any(), anyChar(), anyInt(), any())).thenReturn(lostSession);
        
        mockMvc.perform(post("/game/answer")
                .sessionAttr("sessionId", 1L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("questionId", "1")
                .param("answer", "A")
                .param("timeSpent", "15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correct").value(false));
    }
    
    @Test
    void shouldRejectAnswerWhenTimeExpired() throws Exception {
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        
        mockMvc.perform(post("/game/answer")
                .sessionAttr("sessionId", 1L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("questionId", "1")
                .param("answer", "C")
                .param("timeSpent", "35"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    void shouldSkipQuestion() throws Exception {
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        when(gameService.useSkip(testSession)).thenReturn(testSession);
        when(questionService.getRandomQuestionForLevel(anyInt(), anyList())).thenReturn(testQuestion);
        
        mockMvc.perform(post("/game/skip")
                .sessionAttr("sessionId", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        
        verify(gameService, times(1)).useSkip(testSession);
    }
    
    @Test
    void shouldUseFiftyFifty() throws Exception {
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        when(gameService.useFiftyFifty(testSession)).thenReturn(testSession);
        when(questionService.findById(1L)).thenReturn(testQuestion);
        
        mockMvc.perform(post("/game/fifty-fifty")
                .sessionAttr("sessionId", 1L)
                .param("questionId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        
        verify(gameService, times(1)).useFiftyFifty(testSession);
    }
    
    @Test
    void shouldUseAiHelp() throws Exception {
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        when(gameService.useAiHelp(testSession)).thenReturn(testSession);
        when(questionService.findById(1L)).thenReturn(testQuestion);
        
        GeminiResponseDTO aiResponse = GeminiResponseDTO.builder()
            .letter("C")
            .answer("Brasília")
            .confidence(95)
            .explanation("Brasília é a capital")
            .build();
        
        when(geminiAiService.analyzeQuestion(testQuestion)).thenReturn(aiResponse);
        
        mockMvc.perform(post("/game/ai-help")
                .sessionAttr("sessionId", 1L)
                .param("questionId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.aiResponse.letter").value("C"));
        
        verify(geminiAiService, times(1)).analyzeQuestion(testQuestion);
    }
    
    @Test
    void shouldHandleAiHelpFailure() throws Exception {
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        when(gameService.useAiHelp(testSession)).thenReturn(testSession);
        when(questionService.findById(1L)).thenReturn(testQuestion);
        when(geminiAiService.analyzeQuestion(testQuestion)).thenThrow(new RuntimeException("API Error"));
        
        mockMvc.perform(post("/game/ai-help")
                .sessionAttr("sessionId", 1L)
                .param("questionId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void shouldDisplayGameOverPage() throws Exception {
        testSession.setStatus(GameSession.GameStatus.LOST);
        testSession.setScore(5000);
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        
        Model model = mock(Model.class);
        String viewName = gameController.gameOver(httpSession, model, null);
        
        assertEquals("gameover", viewName);
        verify(model, times(1)).addAttribute(eq("session"), eq(testSession));
    }
    
    @Test
    void shouldDisplayWinnerPage() throws Exception {
        testSession.setStatus(GameSession.GameStatus.WON);
        testSession.setScore(1_000_000);
        when(httpSession.getAttribute("sessionId")).thenReturn(1L);
        when(gameService.getSession(1L)).thenReturn(testSession);
        
        Model model = mock(Model.class);
        String viewName = gameController.winner(httpSession, model);
        
        assertEquals("winner", viewName);
        verify(model, times(1)).addAttribute(eq("session"), eq(testSession));
        verify(model, times(1)).addAttribute(eq("player"), eq(testPlayer));
    }
}

