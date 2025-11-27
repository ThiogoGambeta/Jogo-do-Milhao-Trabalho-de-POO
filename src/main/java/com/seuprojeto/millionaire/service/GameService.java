package com.seuprojeto.millionaire.service;

import com.seuprojeto.millionaire.model.*;
import com.seuprojeto.millionaire.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {
    
    private static final int[] PRIZE_TABLE = {
        1000, 2000, 3000, 4000, 5000,      // Níveis 1-5
        10000, 20000, 30000, 40000, 50000, // Níveis 6-10
        100000, 200000, 300000, 500000, 1000000 // Níveis 11-15
    };
    
    private final GameSessionRepository gameSessionRepository;
    private final QuestionService questionService;
    
    @Transactional
    public GameSession startNewGame(Player player) {
        GameSession session = GameSession.builder()
            .player(player)
            .currentLevel(1)
            .score(0)
            .skipsLeft(3)
            .fiftyFiftyUsed(false)
            .aiHelpUsed(false)
            .status(GameSession.GameStatus.IN_PROGRESS)
            .build();
        
        return gameSessionRepository.save(session);
    }
    
    @Transactional(readOnly = true)
    public GameSession getSession(Long sessionId) {
        // Forçar flush de qualquer operação pendente
        gameSessionRepository.flush();
        // Recarregar a sessão do banco para garantir que temos os dados mais recentes
        // Usar findById novamente para forçar reload do banco
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Sessão de jogo não encontrada"));
        // Forçar detach e reload para garantir dados frescos
        System.out.println("GameService.getSession - Session ID: " + sessionId + ", Current Level: " + session.getCurrentLevel() + ", Score: R$ " + session.getScore() + ", Skips Left: " + session.getSkipsLeft() + ", Answers: " + session.getAnswers().size());
        return session;
    }
    
    @Transactional
    public GameSession submitAnswer(GameSession session, Question question, char answer, 
                                   int timeSpent, Lifeline lifelineUsed) {
        boolean isCorrect = question.isCorrect(answer);
        
        // Criar registro de resposta
        Answer answerRecord = Answer.builder()
            .session(session)
            .question(question)
            .selectedAnswer(answer)
            .correct(isCorrect)
            .timeSpent(timeSpent)
            .lifelineUsed(lifelineUsed != null ? lifelineUsed : Lifeline.NONE)
            .build();
        
        session.getAnswers().add(answerRecord);
        
        if (isCorrect) {
            // Calcular prêmio ANTES de incrementar o nível
            int currentLevelBeforeIncrement = session.getCurrentLevel();
            System.out.println("ACERTOU - Nível ANTES do incremento: " + currentLevelBeforeIncrement);
            session.incrementLevel();
            System.out.println("ACERTOU - Nível DEPOIS do incremento: " + session.getCurrentLevel());
            
            if (session.getCurrentLevel() > 15) {
                // GANHOU O MILHÃO!
                session.setScore(1_000_000);
                session.finishGame(GameSession.GameStatus.WON);
            } else {
                // Atualizar prêmio para o nível que acabou de completar
                int prizeIndex = currentLevelBeforeIncrement - 1; // Nível 1 = índice 0, Nível 2 = índice 1, etc.
                if (prizeIndex >= 0 && prizeIndex < PRIZE_TABLE.length) {
                    int newScore = PRIZE_TABLE[prizeIndex];
                    session.setScore(newScore);
                    System.out.println("ACERTOU - Nível completado: " + currentLevelBeforeIncrement + ", Novo nível: " + session.getCurrentLevel() + ", Prêmio: R$ " + newScore);
                }
            }
        } else {
            // ERROU - o score já deve estar correto (do último acerto)
            // Mas vamos garantir que está usando a parada segura se aplicável
            int lastCompletedLevel = session.getCurrentLevel() - 1;
            
            System.out.println("ERROU - Current Level: " + session.getCurrentLevel() + ", Last Completed: " + lastCompletedLevel);
            System.out.println("ERROU - Score atual antes de recalcular: R$ " + session.getScore());
            
            if (lastCompletedLevel <= 0) {
                // Não completou nenhum nível, ganha R$ 0
                session.setScore(0);
                System.out.println("ERROU - Score definido como R$ 0 (não completou nenhum nível)");
            } else {
                // O score já deve estar correto, mas vamos garantir que está usando a parada segura
                int currentScore = session.getScore();
                int safePrize = calculateSafePrize(lastCompletedLevel);
                
                // Se o score atual é 0 ou menor que a parada segura, recalcular
                if (currentScore == 0 || (safePrize > 0 && currentScore < safePrize)) {
                    // Recalcular prêmio do último nível completado
                    int prizeIndex = lastCompletedLevel - 1;
                    if (prizeIndex >= 0 && prizeIndex < PRIZE_TABLE.length) {
                        int lastPrize = PRIZE_TABLE[prizeIndex];
                        int finalScore = Math.max(lastPrize, safePrize);
                        session.setScore(finalScore);
                        System.out.println("ERROU - Recalculado. Last Prize: R$ " + lastPrize + ", Safe Prize: R$ " + safePrize + ", Final Score: R$ " + finalScore);
                    } else {
                        session.setScore(Math.max(currentScore, safePrize));
                        System.out.println("ERROU - Usando parada segura: R$ " + safePrize);
                    }
                } else {
                    // Score já está correto, só garantir parada segura
                    session.setScore(Math.max(currentScore, safePrize));
                    System.out.println("ERROU - Mantendo score atual: R$ " + session.getScore());
                }
            }
            session.finishGame(GameSession.GameStatus.LOST);
        }
        
        GameSession saved = gameSessionRepository.save(session);
        gameSessionRepository.flush(); // Forçar persistência imediata
        System.out.println("SESSÃO SALVA - ID: " + saved.getId() + ", Score: R$ " + saved.getScore() + ", Level: " + saved.getCurrentLevel());
        // Recarregar para garantir que temos os dados mais recentes
        GameSession refreshed = gameSessionRepository.findById(saved.getId())
            .orElse(saved);
        System.out.println("SESSÃO RECARREGADA - ID: " + refreshed.getId() + ", Score: R$ " + refreshed.getScore() + ", Level: " + refreshed.getCurrentLevel());
        return refreshed;
    }
    
    public int calculateSafePrize(int lastCompletedLevel) {
        // Se não completou nenhum nível, ganha R$ 0
        if (lastCompletedLevel <= 0) return 0;
        // Se completou nível 1-5, ganha R$ 0 (antes da primeira parada segura)
        if (lastCompletedLevel < 5) return 0;
        // Se completou nível 5, ganha R$ 5.000 (primeira parada segura)
        if (lastCompletedLevel == 5) return 5_000;
        // Se completou nível 6-10, ganha R$ 5.000 (mantém primeira parada segura)
        if (lastCompletedLevel <= 10) return 5_000;
        // Se completou nível 10, ganha R$ 50.000 (segunda parada segura)
        if (lastCompletedLevel == 10) return 50_000;
        // Se completou nível 11-15, ganha R$ 50.000 (mantém segunda parada segura)
        return 50_000;
    }
    
    public int[] getPrizeTable() {
        return PRIZE_TABLE;
    }
    
    @Transactional
    public GameSession useSkip(GameSession session) {
        if (session.getSkipsLeft() > 0) {
            session.useSkip();
            GameSession saved = gameSessionRepository.save(session);
            gameSessionRepository.flush();
            System.out.println("GameService.useSkip - Skips restantes: " + saved.getSkipsLeft() + ", Level: " + saved.getCurrentLevel());
            return saved;
        }
        throw new RuntimeException("Sem pulos restantes");
    }
    
    @Transactional
    public GameSession useFiftyFifty(GameSession session) {
        if (session.isFiftyFiftyUsed()) {
            throw new RuntimeException("50:50 já foi usado");
        }
        session.useFiftyFifty();
        GameSession saved = gameSessionRepository.save(session);
        gameSessionRepository.flush();
        System.out.println("GameService.useFiftyFifty - FiftyFiftyUsed salvo: " + saved.isFiftyFiftyUsed());
        // Recarregar para garantir
        GameSession refreshed = gameSessionRepository.findById(saved.getId()).orElse(saved);
        System.out.println("GameService.useFiftyFifty - FiftyFiftyUsed após reload: " + refreshed.isFiftyFiftyUsed());
        return refreshed;
    }
    
    @Transactional
    public GameSession saveSession(GameSession session) {
        GameSession saved = gameSessionRepository.save(session);
        gameSessionRepository.flush();
        return saved;
    }
    
    @Transactional
    public GameSession useAiHelp(GameSession session) {
        if (!session.isAiHelpUsed()) {
            session.useAiHelp();
            return gameSessionRepository.save(session);
        }
        throw new RuntimeException("Ajuda da IA já foi utilizada");
    }
}

