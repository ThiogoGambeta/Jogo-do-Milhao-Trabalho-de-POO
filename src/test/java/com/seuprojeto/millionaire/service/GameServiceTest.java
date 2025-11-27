package com.seuprojeto.millionaire.service;

import com.seuprojeto.millionaire.model.*;
import com.seuprojeto.millionaire.repository.GameSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {
    
    @Mock
    private GameSessionRepository gameSessionRepository;
    
    @Mock
    private QuestionService questionService;
    
    @InjectMocks
    private GameService gameService;
    
    private Player testPlayer;
    private GameSession testSession;
    
    @BeforeEach
    void setUp() {
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
            .build();
    }
    
    @Test
    void shouldStartNewGameSuccessfully() {
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);
        
        GameSession session = gameService.startNewGame(testPlayer);
        
        assertNotNull(session);
        assertEquals(1, session.getCurrentLevel());
        assertEquals(3, session.getSkipsLeft());
        assertFalse(session.isFiftyFiftyUsed());
        assertFalse(session.isAiHelpUsed());
        
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }
    
    @Test
    void shouldDecrementSkipsWhenUsed() {
        testSession.useSkip();
        
        assertEquals(2, testSession.getSkipsLeft());
    }
    
    @Test
    void shouldNotDecrementSkipsWhenNoneLeft() {
        testSession.setSkipsLeft(0);
        testSession.useSkip();
        
        assertEquals(0, testSession.getSkipsLeft());
    }
    
    @Test
    void shouldCalculateSafePrizeCorrectly() {
        // Nível 1-5: R$ 0
        assertEquals(0, gameService.calculateSafePrize(3));
        
        // Nível 6-10: R$ 5.000
        assertEquals(5000, gameService.calculateSafePrize(8));
        
        // Nível 11-15: R$ 50.000
        assertEquals(50000, gameService.calculateSafePrize(13));
    }
    
    @Test
    void shouldGetSessionSuccessfully() {
        when(gameSessionRepository.findById(1L)).thenReturn(java.util.Optional.of(testSession));
        
        GameSession session = gameService.getSession(1L);
        
        assertNotNull(session);
        assertEquals(1L, session.getId());
        verify(gameSessionRepository, times(1)).findById(1L);
    }
    
    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        when(gameSessionRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        
        assertThrows(RuntimeException.class, () -> gameService.getSession(999L));
    }
    
    @Test
    void shouldSubmitCorrectAnswerAndIncrementLevel() {
        Question question = Question.builder()
            .id(1L)
            .questionText("Teste")
            .optionA("A")
            .optionB("B")
            .optionC("C")
            .optionD("D")
            .correctAnswer('C')
            .level(1)
            .build();
        
        testSession.setCurrentLevel(1);
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);
        
        GameSession result = gameService.submitAnswer(testSession, question, 'C', 10, Lifeline.NONE);
        
        assertEquals(2, result.getCurrentLevel());
        assertEquals(1000, result.getScore());
        assertTrue(result.getAnswers().stream().anyMatch(a -> a.isCorrect()));
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }
    
    @Test
    void shouldSubmitWrongAnswerAndFinishGame() {
        Question question = Question.builder()
            .id(1L)
            .questionText("Teste")
            .optionA("A")
            .optionB("B")
            .optionC("C")
            .optionD("D")
            .correctAnswer('C')
            .level(6)
            .build();
        
        testSession.setCurrentLevel(6);
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);
        
        GameSession result = gameService.submitAnswer(testSession, question, 'A', 15, Lifeline.NONE);
        
        assertEquals(GameSession.GameStatus.LOST, result.getStatus());
        assertEquals(5000, result.getScore());
        assertTrue(result.getAnswers().stream().anyMatch(a -> !a.isCorrect()));
    }
    
    @Test
    void shouldWinMillionWhenReachingLevel15() {
        Question question = Question.builder()
            .id(1L)
            .questionText("Teste")
            .optionA("A")
            .optionB("B")
            .optionC("C")
            .optionD("D")
            .correctAnswer('C')
            .level(15)
            .build();
        
        testSession.setCurrentLevel(15);
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);
        
        GameSession result = gameService.submitAnswer(testSession, question, 'C', 20, Lifeline.NONE);
        
        assertEquals(GameSession.GameStatus.WON, result.getStatus());
        assertEquals(1_000_000, result.getScore());
    }
    
    @Test
    void shouldUseSkipSuccessfully() {
        testSession.setSkipsLeft(2);
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);
        
        GameSession result = gameService.useSkip(testSession);
        
        assertEquals(1, result.getSkipsLeft());
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }
    
    @Test
    void shouldThrowExceptionWhenNoSkipsLeft() {
        testSession.setSkipsLeft(0);
        
        assertThrows(RuntimeException.class, () -> gameService.useSkip(testSession));
        verify(gameSessionRepository, never()).save(any(GameSession.class));
    }
    
    @Test
    void shouldUseFiftyFiftySuccessfully() {
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);
        
        GameSession result = gameService.useFiftyFifty(testSession);
        
        assertTrue(result.isFiftyFiftyUsed());
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }
    
    @Test
    void shouldThrowExceptionWhenFiftyFiftyAlreadyUsed() {
        testSession.setFiftyFiftyUsed(true);
        
        assertThrows(RuntimeException.class, () -> gameService.useFiftyFifty(testSession));
        verify(gameSessionRepository, never()).save(any(GameSession.class));
    }
    
    @Test
    void shouldUseAiHelpSuccessfully() {
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);
        
        GameSession result = gameService.useAiHelp(testSession);
        
        assertTrue(result.isAiHelpUsed());
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
    }
    
    @Test
    void shouldThrowExceptionWhenAiHelpAlreadyUsed() {
        testSession.setAiHelpUsed(true);
        
        assertThrows(RuntimeException.class, () -> gameService.useAiHelp(testSession));
        verify(gameSessionRepository, never()).save(any(GameSession.class));
    }
    
    @Test
    void shouldGetPrizeTable() {
        int[] prizeTable = gameService.getPrizeTable();
        
        assertNotNull(prizeTable);
        assertEquals(15, prizeTable.length);
        assertEquals(1000, prizeTable[0]);
        assertEquals(1_000_000, prizeTable[14]);
    }
}

