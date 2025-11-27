package com.seuprojeto.millionaire.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seuprojeto.millionaire.dto.GeminiResponseDTO;
import com.seuprojeto.millionaire.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiAiServiceTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private GeminiAiService geminiAiService;
    
    private Question testQuestion;
    private String mockJsonResponse;
    
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
            .build();
        
        mockJsonResponse = """
            {
              "letter": "C",
              "answer": "Brasília",
              "confidence": 95,
              "explanation": "Brasília é a capital federal desde 1960"
            }
            """;
    }
    
    @Test
    void shouldUseFallbackWhenExceptionOccurs() {
        GeminiResponseDTO result = geminiAiService.fallbackResponse(testQuestion, new RuntimeException("Error"));
        
        assertNotNull(result);
        assertEquals("?", result.getLetter());
        assertEquals(0, result.getConfidence());
        assertTrue(result.getExplanation().contains("indisponível"));
    }
    
    @Test
    void shouldReturnValidResponseStructure() {
        GeminiResponseDTO fallback = geminiAiService.fallbackResponse(testQuestion, new RuntimeException("Error"));
        
        assertNotNull(fallback.getLetter());
        assertNotNull(fallback.getAnswer());
        assertNotNull(fallback.getExplanation());
        assertTrue(fallback.getConfidence() >= 0 && fallback.getConfidence() <= 100);
    }
}

