package com.seuprojeto.millionaire.controller;

import com.seuprojeto.millionaire.model.GameSession;
import com.seuprojeto.millionaire.model.Player;
import com.seuprojeto.millionaire.service.GameService;
import com.seuprojeto.millionaire.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlayerControllerTest {
    
    private MockMvc mockMvc;
    private PlayerService playerService;
    private GameService gameService;
    private PlayerController playerController;
    
    private Player testPlayer;
    private GameSession testSession;
    
    @BeforeEach
    void setUp() {
        playerService = mock(PlayerService.class);
        gameService = mock(GameService.class);
        playerController = new PlayerController(playerService, gameService);
        mockMvc = MockMvcBuilders.standaloneSetup(playerController).build();
        
        testPlayer = Player.builder()
            .id(1L)
            .nickname("TestPlayer")
            .build();
        
        testSession = GameSession.builder()
            .id(1L)
            .player(testPlayer)
            .currentLevel(1)
            .score(0)
            .build();
    }
    
    @Test
    void shouldRegisterPlayerSuccessfully() throws Exception {
        when(playerService.findOrCreatePlayer("TestPlayer")).thenReturn(testPlayer);
        when(gameService.startNewGame(testPlayer)).thenReturn(testSession);
        
        mockMvc.perform(post("/player/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "TestPlayer"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.redirectUrl").value("/game"));
        
        verify(playerService, times(1)).findOrCreatePlayer("TestPlayer");
        verify(gameService, times(1)).startNewGame(testPlayer);
    }
    
    @Test
    void shouldRejectShortNickname() throws Exception {
        mockMvc.perform(post("/player/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "AB"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
        
        verify(playerService, never()).findOrCreatePlayer(any());
        verify(gameService, never()).startNewGame(any());
    }
    
    @Test
    void shouldTrimNickname() throws Exception {
        when(playerService.findOrCreatePlayer("TestPlayer")).thenReturn(testPlayer);
        when(gameService.startNewGame(testPlayer)).thenReturn(testSession);
        
        mockMvc.perform(post("/player/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "  TestPlayer  "))
            .andExpect(status().isOk());
        
        verify(playerService, times(1)).findOrCreatePlayer("TestPlayer");
    }
    
    @Test
    void shouldHandleServiceException() throws Exception {
        when(playerService.findOrCreatePlayer("TestPlayer")).thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(post("/player/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "TestPlayer"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }
}

