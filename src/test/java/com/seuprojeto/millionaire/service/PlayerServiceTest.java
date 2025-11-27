package com.seuprojeto.millionaire.service;

import com.seuprojeto.millionaire.model.Player;
import com.seuprojeto.millionaire.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {
    
    @Mock
    private PlayerRepository playerRepository;
    
    @InjectMocks
    private PlayerService playerService;
    
    private Player testPlayer;
    
    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
            .id(1L)
            .nickname("TestPlayer")
            .build();
    }
    
    @Test
    void shouldFindExistingPlayer() {
        when(playerRepository.findByNickname("TestPlayer")).thenReturn(Optional.of(testPlayer));
        
        Player result = playerService.findOrCreatePlayer("TestPlayer");
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TestPlayer", result.getNickname());
        verify(playerRepository, times(1)).findByNickname("TestPlayer");
        verify(playerRepository, never()).save(any(Player.class));
    }
    
    @Test
    void shouldCreateNewPlayerWhenNotFound() {
        when(playerRepository.findByNickname("NewPlayer")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
        
        Player result = playerService.findOrCreatePlayer("NewPlayer");
        
        assertNotNull(result);
        verify(playerRepository, times(1)).findByNickname("NewPlayer");
        verify(playerRepository, times(1)).save(any(Player.class));
    }
    
    @Test
    void shouldTrimNickname() {
        when(playerRepository.findByNickname("TestPlayer")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player p = invocation.getArgument(0);
            assertEquals("TestPlayer", p.getNickname());
            return p;
        });
        
        playerService.findOrCreatePlayer("  TestPlayer  ");
        
        verify(playerRepository, times(1)).findByNickname("TestPlayer");
    }
    
    @Test
    void shouldFindPlayerById() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        
        Player result = playerService.findById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(playerRepository, times(1)).findById(1L);
    }
    
    @Test
    void shouldThrowExceptionWhenPlayerNotFound() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> playerService.findById(999L));
    }
}

