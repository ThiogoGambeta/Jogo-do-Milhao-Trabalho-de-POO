package com.seuprojeto.millionaire.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_sessions", indexes = {
    @Index(name = "idx_player_id", columnList = "player_id"),
    @Index(name = "idx_started_at", columnList = "started_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @Column(nullable = false)
    @Builder.Default
    private int currentLevel = 1;
    
    @Column(nullable = false)
    @Builder.Default
    private int score = 0; // Prêmio acumulado em reais
    
    @Column(nullable = false)
    @Builder.Default
    private int skipsLeft = 3;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean fiftyFiftyUsed = false;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean aiHelpUsed = false;
    
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GameStatus status = GameStatus.IN_PROGRESS;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }
    
    // Métodos auxiliares
    public void incrementLevel() {
        this.currentLevel++;
    }
    
    public void useSkip() {
        if (skipsLeft > 0) {
            skipsLeft--;
        }
    }
    
    public void useFiftyFifty() {
        fiftyFiftyUsed = true;
    }
    
    public void useAiHelp() {
        aiHelpUsed = true;
    }
    
    public void finishGame(GameStatus finalStatus) {
        this.status = finalStatus;
        this.finishedAt = LocalDateTime.now();
    }
    
    public enum GameStatus {
        IN_PROGRESS,
        WON,
        LOST,
        QUIT
    }
}

