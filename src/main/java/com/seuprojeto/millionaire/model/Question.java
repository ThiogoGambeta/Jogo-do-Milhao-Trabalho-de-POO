package com.seuprojeto.millionaire.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions", indexes = {
    @Index(name = "idx_level", columnList = "level"),
    @Index(name = "idx_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Column(nullable = false, length = 500)
    private String optionA;
    
    @Column(nullable = false, length = 500)
    private String optionB;
    
    @Column(nullable = false, length = 500)
    private String optionC;
    
    @Column(nullable = false, length = 500)
    private String optionD;
    
    @Column(nullable = false, length = 1)
    private char correctAnswer; // 'A', 'B', 'C' ou 'D'
    
    @Column(nullable = false)
    private int level; // 1 a 15
    
    @Column(length = 100)
    private String category; // História, Geografia, Ciências, Cultura Pop, etc
    
    @Column(columnDefinition = "TEXT")
    private String explanation; // Explicação da resposta correta (opcional)
    
    // Método auxiliar para validação
    public boolean isCorrect(char answer) {
        return Character.toUpperCase(answer) == Character.toUpperCase(correctAnswer);
    }
}

