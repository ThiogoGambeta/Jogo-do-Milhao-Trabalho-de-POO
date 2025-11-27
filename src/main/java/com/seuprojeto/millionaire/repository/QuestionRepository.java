package com.seuprojeto.millionaire.repository;

import com.seuprojeto.millionaire.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    List<Question> findByLevel(int level);
    
    @Query("SELECT q FROM Question q WHERE q.level = :level AND q.id NOT IN :excludedIds")
    List<Question> findByLevelExcludingIds(@Param("level") int level, @Param("excludedIds") List<Long> excludedIds);
}

