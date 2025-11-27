package com.seuprojeto.millionaire.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "answers", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(nullable = false, length = 1)
    private char selectedAnswer; // 'A', 'B', 'C' ou 'D'
    
    @Column(nullable = false)
    private boolean correct;
    
    @Column(nullable = false)
    private int timeSpent; // Segundos gastos para responder
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Lifeline lifelineUsed; // Qual coringa foi usado, se algum
    
    @Column(name = "answered_at", nullable = false, updatable = false)
    private LocalDateTime answeredAt;
    
    @PrePersist
    protected void onCreate() {
        answeredAt = LocalDateTime.now();
    }
}

