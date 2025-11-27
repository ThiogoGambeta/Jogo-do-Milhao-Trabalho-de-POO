package com.seuprojeto.millionaire.repository;

import com.seuprojeto.millionaire.model.Question;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class QuestionRepositoryTest {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Test
    void shouldFindQuestionsByLevel() {
        List<Question> questions = questionRepository.findByLevel(1);
        
        assertNotNull(questions);
        assertFalse(questions.isEmpty());
        questions.forEach(q -> assertEquals(1, q.getLevel()));
    }
    
    @Test
    void shouldFindQuestionsExcludingIds() {
        List<Question> allQuestions = questionRepository.findByLevel(1);
        assertFalse(allQuestions.isEmpty());
        
        Long firstId = allQuestions.get(0).getId();
        List<Question> excludedQuestions = questionRepository.findByLevelExcludingIds(1, List.of(firstId));
        
        assertNotNull(excludedQuestions);
        excludedQuestions.forEach(q -> {
            assertEquals(1, q.getLevel());
            assertNotEquals(firstId, q.getId());
        });
    }
    
    @Test
    void shouldSaveAndRetrieveQuestion() {
        Question newQuestion = Question.builder()
            .questionText("Nova pergunta?")
            .optionA("Opção A")
            .optionB("Opção B")
            .optionC("Opção C")
            .optionD("Opção D")
            .correctAnswer('A')
            .level(1)
            .category("Teste")
            .build();
        
        Question saved = questionRepository.save(newQuestion);
        assertNotNull(saved.getId());
        
        Question found = questionRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Nova pergunta?", found.getQuestionText());
    }
}

