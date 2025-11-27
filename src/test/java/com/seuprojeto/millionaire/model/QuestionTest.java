package com.seuprojeto.millionaire.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestionTest {
    
    private Question question;
    
    @BeforeEach
    void setUp() {
        question = Question.builder()
            .id(1L)
            .questionText("Qual é a capital do Brasil?")
            .optionA("São Paulo")
            .optionB("Rio de Janeiro")
            .optionC("Brasília")
            .optionD("Salvador")
            .correctAnswer('C')
            .level(1)
            .category("Geografia")
            .explanation("Brasília é a capital federal")
            .build();
    }
    
    @Test
    void shouldReturnTrueForCorrectAnswer() {
        assertTrue(question.isCorrect('C'));
        assertTrue(question.isCorrect('c')); // Case insensitive
    }
    
    @Test
    void shouldReturnFalseForWrongAnswer() {
        assertFalse(question.isCorrect('A'));
        assertFalse(question.isCorrect('B'));
        assertFalse(question.isCorrect('D'));
    }
    
    @Test
    void shouldHandleCaseInsensitiveComparison() {
        question.setCorrectAnswer('A');
        assertTrue(question.isCorrect('a'));
        assertTrue(question.isCorrect('A'));
    }
}

