package com.seuprojeto.millionaire.service;

import com.seuprojeto.millionaire.model.Question;
import com.seuprojeto.millionaire.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {
    
    @Mock
    private QuestionRepository questionRepository;
    
    @InjectMocks
    private QuestionService questionService;
    
    private Question testQuestion;
    
    @BeforeEach
    void setUp() {
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
            .explanation("Brasília é a capital federal desde 1960")
            .build();
    }
    
    @Test
    void shouldFindQuestionById() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(testQuestion));
        
        Question result = questionService.findById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Qual é a capital do Brasil?", result.getQuestionText());
        verify(questionRepository, times(1)).findById(1L);
    }
    
    @Test
    void shouldThrowExceptionWhenQuestionNotFound() {
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> questionService.findById(999L));
    }
    
    @Test
    void shouldGetRandomQuestionForLevel() {
        List<Question> questions = Arrays.asList(
            testQuestion,
            Question.builder().id(2L).level(1).questionText("Q2").optionA("A").optionB("B").optionC("C").optionD("D").correctAnswer('A').build()
        );
        
        when(questionRepository.findByLevel(1)).thenReturn(questions);
        
        Question result = questionService.getRandomQuestionForLevel(1, Collections.emptyList());
        
        assertNotNull(result);
        assertEquals(1, result.getLevel());
        verify(questionRepository, times(1)).findByLevel(1);
    }
    
    @Test
    void shouldGetRandomQuestionExcludingIds() {
        List<Question> questions = Arrays.asList(
            Question.builder().id(2L).level(1).questionText("Q2").optionA("A").optionB("B").optionC("C").optionD("D").correctAnswer('A').build(),
            Question.builder().id(3L).level(1).questionText("Q3").optionA("A").optionB("B").optionC("C").optionD("D").correctAnswer('B').build()
        );
        
        when(questionRepository.findByLevelExcludingIds(1, Arrays.asList(1L))).thenReturn(questions);
        
        Question result = questionService.getRandomQuestionForLevel(1, Arrays.asList(1L));
        
        assertNotNull(result);
        assertEquals(1, result.getLevel());
        assertNotEquals(1L, result.getId());
        verify(questionRepository, times(1)).findByLevelExcludingIds(1, Arrays.asList(1L));
    }
    
    @Test
    void shouldFallbackToAllQuestionsWhenExcludedListIsEmpty() {
        List<Question> questions = Arrays.asList(testQuestion);
        
        when(questionRepository.findByLevel(1)).thenReturn(questions);
        
        Question result = questionService.getRandomQuestionForLevel(1, null);
        
        assertNotNull(result);
        verify(questionRepository, times(1)).findByLevel(1);
    }
    
    @Test
    void shouldFallbackToAllQuestionsWhenNoExcludedQuestionsFound() {
        when(questionRepository.findByLevelExcludingIds(1, Arrays.asList(1L))).thenReturn(Collections.emptyList());
        when(questionRepository.findByLevel(1)).thenReturn(Arrays.asList(testQuestion));
        
        Question result = questionService.getRandomQuestionForLevel(1, Arrays.asList(1L));
        
        assertNotNull(result);
        verify(questionRepository, times(1)).findByLevel(1);
    }
    
    @Test
    void shouldThrowExceptionWhenNoQuestionsAvailable() {
        when(questionRepository.findByLevel(1)).thenReturn(Collections.emptyList());
        when(questionRepository.findByLevelExcludingIds(1, Collections.emptyList())).thenReturn(Collections.emptyList());
        
        assertThrows(RuntimeException.class, () -> questionService.getRandomQuestionForLevel(1, Collections.emptyList()));
    }
}

