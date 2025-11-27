package com.seuprojeto.millionaire.service;

import com.seuprojeto.millionaire.model.Question;
import com.seuprojeto.millionaire.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    private final Random random = new Random();
    
    public Question findById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pergunta não encontrada"));
    }
    
    public Question getRandomQuestionForLevel(int level, List<Long> excludedIds) {
        List<Question> questions;
        
        if (excludedIds == null || excludedIds.isEmpty()) {
            questions = questionRepository.findByLevel(level);
        } else {
            questions = questionRepository.findByLevelExcludingIds(level, excludedIds);
        }
        
        if (questions.isEmpty()) {
            // Fallback: buscar qualquer pergunta do nível se não houver mais disponíveis
            questions = questionRepository.findByLevel(level);
        }
        
        if (questions.isEmpty()) {
            throw new RuntimeException("Nenhuma pergunta disponível para o nível " + level);
        }
        
        Collections.shuffle(questions, random);
        return questions.get(0);
    }
}

