package com.seuprojeto.millionaire.service;

import com.seuprojeto.millionaire.model.GameSession;
import com.seuprojeto.millionaire.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {
    
    private final GameSessionRepository gameSessionRepository;
    
    public List<GameSession> getTop10Ranking() {
        return gameSessionRepository.findTop10ByStatusNotOrderByScoreDescStartedAtDesc(
            GameSession.GameStatus.IN_PROGRESS
        );
    }
}

