package com.seuprojeto.millionaire.repository;

import com.seuprojeto.millionaire.model.GameSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    
    @Query("SELECT gs FROM GameSession gs WHERE gs.status != :status ORDER BY gs.score DESC, gs.startedAt DESC")
    List<GameSession> findTop10ByStatusNotOrderByScoreDescStartedAtDesc(@Param("status") GameSession.GameStatus status, Pageable pageable);
    
    default List<GameSession> findTop10ByStatusNotOrderByScoreDescStartedAtDesc(GameSession.GameStatus status) {
        return findTop10ByStatusNotOrderByScoreDescStartedAtDesc(status, org.springframework.data.domain.PageRequest.of(0, 10));
    }
}

