package com.seuprojeto.millionaire.controller;

import com.seuprojeto.millionaire.dto.GeminiResponseDTO;
import com.seuprojeto.millionaire.model.*;
import com.seuprojeto.millionaire.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
@Slf4j
public class GameController {
    
    private final GameService gameService;
    private final QuestionService questionService;
    private final GeminiAiService geminiAiService;
    
    @GetMapping
    public String gamePage(HttpSession httpSession, Model model) {
        Long sessionId = (Long) httpSession.getAttribute("sessionId");
        if (sessionId == null) {
            return "redirect:/";
        }
        GameSession session = gameService.getSession(sessionId);
        
        // Log para debug
        System.out.println("GameController.gamePage - Session ID: " + sessionId);
        System.out.println("GameController.gamePage - Current Level (do banco): " + session.getCurrentLevel());
        System.out.println("GameController.gamePage - Score: R$ " + session.getScore());
        System.out.println("GameController.gamePage - Skips Left: " + session.getSkipsLeft());
        System.out.println("GameController.gamePage - FiftyFiftyUsed: " + session.isFiftyFiftyUsed());
        System.out.println("GameController.gamePage - AiHelpUsed: " + session.isAiHelpUsed());
        System.out.println("GameController.gamePage - Answers count: " + session.getAnswers().size());
        
        // SEMPRE calcular o nível correto baseado no número de respostas corretas
        long correctAnswersCount = session.getAnswers().stream()
            .filter(a -> a.isCorrect())
            .count();
        int calculatedLevel = (int) correctAnswersCount + 1; // Se tem 0 corretas, está no nível 1
        
        System.out.println("GameController.gamePage - Respostas corretas: " + correctAnswersCount + ", Nível calculado: " + calculatedLevel);
        System.out.println("GameController.gamePage - Nível do banco: " + session.getCurrentLevel());
        
        // SEMPRE usar o nível calculado, não confiar no banco
        if (session.getCurrentLevel() != calculatedLevel) {
            System.out.println("GameController.gamePage - Corrigindo nível: " + session.getCurrentLevel() + " -> " + calculatedLevel);
            session.setCurrentLevel(calculatedLevel);
            session = gameService.saveSession(session);
            // Recarregar a sessão para garantir que temos os dados atualizados
            session = gameService.getSession(sessionId);
        }
        
        // Garantir que currentLevel nunca seja null ou 0
        if (session.getCurrentLevel() <= 0) {
            System.out.println("GameController.gamePage - WARNING: CurrentLevel <= 0, ajustando para 1");
            session.setCurrentLevel(1);
            session = gameService.saveSession(session);
            session = gameService.getSession(sessionId);
        }
        
        // Log final - IMPORTANTE: garantir que o valor está correto
        System.out.println("GameController.gamePage - NÍVEL FINAL PARA TEMPLATE: " + session.getCurrentLevel() + ", SKIPS: " + session.getSkipsLeft());
        
        if (session.getStatus() != GameSession.GameStatus.IN_PROGRESS) {
            if (session.getStatus() == GameSession.GameStatus.WON) {
                return "redirect:/game/winner";
            }
            return "redirect:/game/gameover";
        }
        
        List<Long> answeredQuestionIds = session.getAnswers().stream()
            .map(a -> a.getQuestion().getId())
            .collect(Collectors.toList());
        
        Question question = questionService.getRandomQuestionForLevel(
            session.getCurrentLevel(),
            answeredQuestionIds
        );
        
        if (question == null) {
            System.err.println("ERRO: Não foi possível encontrar uma pergunta para o nível " + session.getCurrentLevel());
            return "redirect:/game/gameover";
        }
        
        // Inverter o array de prêmios para exibir do maior (topo) para o menor (base)
        int[] prizeTable = gameService.getPrizeTable();
        int[] reversedPrizeTable = new int[prizeTable.length];
        for (int i = 0; i < prizeTable.length; i++) {
            reversedPrizeTable[i] = prizeTable[prizeTable.length - 1 - i];
        }
        
        // Garantir que os valores estão corretos antes de passar para o template
        System.out.println("GameController.gamePage - PASSANDO PARA TEMPLATE:");
        System.out.println("  - Level: " + session.getCurrentLevel());
        System.out.println("  - Skips: " + session.getSkipsLeft());
        System.out.println("  - Score: R$ " + session.getScore());
        System.out.println("  - FiftyFiftyUsed: " + session.isFiftyFiftyUsed());
        System.out.println("  - AiHelpUsed: " + session.isAiHelpUsed());
        System.out.println("  - Answers corretas: " + correctAnswersCount);
        
        // Passar também os valores explicitamente para garantir
        model.addAttribute("currentLevel", session.getCurrentLevel());
        model.addAttribute("skipsLeft", session.getSkipsLeft());
        model.addAttribute("session", session);
        model.addAttribute("question", question);
        model.addAttribute("prizeTable", reversedPrizeTable);
        
        return "game";
    }
    
    @PostMapping("/answer")
    public ResponseEntity<?> submitAnswer(
            HttpSession httpSession,
            @RequestParam Long questionId,
            @RequestParam char answer,
            @RequestParam int timeSpent,
            @RequestParam(required = false) String lifelineUsed) {
        
        try {
            Long sessionId = (Long) httpSession.getAttribute("sessionId");
            if (sessionId == null) {
                System.out.println("ERRO: SessionId é null");
                return ResponseEntity.badRequest().body(Map.of("error", "Sessão não encontrada"));
            }
            
            GameSession session = gameService.getSession(sessionId);
            if (session == null) {
                System.out.println("ERRO: Sessão não encontrada no banco. ID: " + sessionId);
                return ResponseEntity.badRequest().body(Map.of("error", "Sessão de jogo não encontrada"));
            }
            
            Question question = questionService.findById(questionId);
            if (question == null) {
                System.out.println("ERRO: Pergunta não encontrada. ID: " + questionId);
                return ResponseEntity.badRequest().body(Map.of("error", "Pergunta não encontrada"));
            }
            
            // Validar resposta (não aceitar 'Z' ou caracteres inválidos)
            char validAnswer = Character.toUpperCase(answer);
            if (validAnswer != 'A' && validAnswer != 'B' && validAnswer != 'C' && validAnswer != 'D') {
                System.out.println("ERRO: Resposta inválida: " + answer);
                // Tratar como timeout/erro
                validAnswer = 'A'; // Resposta padrão que será errada
            }
            
            // Validar tempo
            if (timeSpent > 30) {
                System.out.println("ERRO: Tempo excedido: " + timeSpent);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tempo expirado"
                ));
            }
            
            System.out.println("Processando resposta - SessionId: " + sessionId + ", QuestionId: " + questionId + ", Answer: " + validAnswer + ", TimeSpent: " + timeSpent);
            
            Lifeline lifeline = lifelineUsed != null ? Lifeline.valueOf(lifelineUsed) : Lifeline.NONE;
            session = gameService.submitAnswer(session, question, validAnswer, timeSpent, lifeline);
            
            boolean isCorrect = question.isCorrect(validAnswer);
            System.out.println("Resposta processada - Correta: " + isCorrect + ", Novo Level: " + session.getCurrentLevel() + ", Score: " + session.getScore());
            
            if (isCorrect) {
                if (session.getStatus() == GameSession.GameStatus.WON) {
                return ResponseEntity.ok(Map.of(
                    "correct", true,
                    "won", true,
                    "prize", 1_000_000,
                    "redirectUrl", "/game/winner"
                ));
                }
                
                return ResponseEntity.ok(Map.of(
                    "correct", true,
                    "currentLevel", session.getCurrentLevel(),
                    "currentPrize", session.getScore(),
                    "nextQuestionUrl", "/game"
                ));
            } else {
                // Garantir que o score está correto antes de retornar
                int finalScore = session.getScore();
                System.out.println("Controller - Resposta errada. Score final: R$ " + finalScore);
                return ResponseEntity.ok(Map.of(
                    "correct", false,
                    "correctAnswer", String.valueOf(question.getCorrectAnswer()),
                    "explanation", question.getExplanation() != null ? question.getExplanation() : "",
                    "finalPrize", finalScore,
                    "redirectUrl", "/game/gameover"
                ));
            }
        } catch (Exception e) {
            System.err.println("ERRO ao processar resposta:");
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.status(500).body(Map.of(
                "error", "Erro interno: " + errorMessage
            ));
        }
    }
    
    @PostMapping("/skip")
    public ResponseEntity<?> skipQuestion(HttpSession httpSession) {
        try {
            Long sessionId = (Long) httpSession.getAttribute("sessionId");
            if (sessionId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sessão não encontrada"));
            }
            GameSession session = gameService.getSession(sessionId);
            session = gameService.useSkip(session);
            
            List<Long> answeredQuestionIds = session.getAnswers().stream()
                .map(a -> a.getQuestion().getId())
                .collect(Collectors.toList());
            
            Question newQuestion = questionService.getRandomQuestionForLevel(
                session.getCurrentLevel(),
                answeredQuestionIds
            );
            
            // Recarregar a sessão para garantir valores atualizados
            session = gameService.getSession(sessionId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "newQuestion", Map.of(
                    "id", newQuestion.getId(),
                    "questionText", newQuestion.getQuestionText(),
                    "optionA", newQuestion.getOptionA(),
                    "optionB", newQuestion.getOptionB(),
                    "optionC", newQuestion.getOptionC(),
                    "optionD", newQuestion.getOptionD(),
                    "level", newQuestion.getLevel(),
                    "category", newQuestion.getCategory()
                ),
                "skipsLeft", session.getSkipsLeft(),
                "currentLevel", session.getCurrentLevel()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/fifty-fifty")
    public ResponseEntity<?> useFiftyFifty(
            HttpSession httpSession,
            @RequestParam Long questionId) {
        
        try {
            Long sessionId = (Long) httpSession.getAttribute("sessionId");
            if (sessionId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sessão não encontrada"));
            }
            GameSession session = gameService.getSession(sessionId);
            Question question = questionService.findById(questionId);
            
            System.out.println("GameController.useFiftyFifty - Antes: fiftyFiftyUsed = " + session.isFiftyFiftyUsed());
            session = gameService.useFiftyFifty(session);
            System.out.println("GameController.useFiftyFifty - Depois: fiftyFiftyUsed = " + session.isFiftyFiftyUsed());
            
            // Recarregar a sessão para garantir valores atualizados
            session = gameService.getSession(sessionId);
            System.out.println("GameController.useFiftyFifty - Após reload: fiftyFiftyUsed = " + session.isFiftyFiftyUsed());
            
            // Lógica para escolher 2 erradas para remover
            char correct = question.getCorrectAnswer();
            List<Character> options = Arrays.asList('A', 'B', 'C', 'D');
            List<Character> wrongOptions = options.stream()
                .filter(c -> c != correct)
                .collect(Collectors.toList());
            
            Collections.shuffle(wrongOptions);
            List<Character> toRemove = wrongOptions.subList(0, 2);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "removedOptions", toRemove.stream().map(String::valueOf).collect(Collectors.toList())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/ai-help")
    public ResponseEntity<?> useAiHelp(
            HttpSession httpSession,
            @RequestParam Long questionId) {
        
        try {
            Long sessionId = (Long) httpSession.getAttribute("sessionId");
            if (sessionId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sessão não encontrada"));
            }
            GameSession session = gameService.getSession(sessionId);
            
            // Verificar se a ajuda da IA já foi usada ANTES de tentar usar novamente
            if (session.isAiHelpUsed()) {
                log.info("Tentativa de usar ajuda da IA quando já foi utilizada. Session ID: {}", sessionId);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "alreadyUsed", true,
                    "message", "Você já utilizou a ajuda da IA nesta partida. Esta ajuda só pode ser usada uma vez!"
                ));
            }
            
            Question question = questionService.findById(questionId);
            
            session = gameService.useAiHelp(session);
            
            GeminiResponseDTO aiResponse = geminiAiService.analyzeQuestion(question);
            
            // Verificar se é uma resposta de fallback (confiança 0 e letra "?")
            boolean isFallback = aiResponse.getConfidence() == 0 && "?".equals(aiResponse.getLetter());
            
            if (isFallback) {
                log.warn("Resposta de fallback retornada para questão ID: {}", questionId);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", aiResponse.getExplanation(),
                    "aiResponse", Map.of(
                        "letter", aiResponse.getLetter(),
                        "answer", aiResponse.getAnswer(),
                        "confidence", aiResponse.getConfidence(),
                        "explanation", aiResponse.getExplanation()
                    )
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "aiResponse", Map.of(
                    "letter", aiResponse.getLetter(),
                    "answer", aiResponse.getAnswer(),
                    "confidence", aiResponse.getConfidence(),
                    "explanation", aiResponse.getExplanation()
                )
            ));
        } catch (RuntimeException e) {
            // Verificar se é o erro de "já foi usado"
            if (e.getMessage() != null && e.getMessage().contains("já foi utilizada")) {
                log.info("Ajuda da IA já foi utilizada. Session ID: {}", httpSession.getAttribute("sessionId"));
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "alreadyUsed", true,
                    "message", "Você já utilizou a ajuda da IA nesta partida. Esta ajuda só pode ser usada uma vez!"
                ));
            }
            
            log.error("Erro ao usar ajuda da IA para questão ID: {}", questionId, e);
            String errorMessage = "A IA está temporariamente indisponível. Tente resolver sozinho!";
            String detailedError = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            
            if (detailedError.contains("API") || detailedError.contains("HTTP") || detailedError.contains("401") || detailedError.contains("403") || detailedError.contains("404")) {
                errorMessage = "Erro na comunicação com a IA. Verifique a chave da API.";
                log.error("Erro de API detectado: {}", detailedError);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", errorMessage,
                "error", detailedError
            ));
        } catch (Exception e) {
            log.error("Erro inesperado ao usar ajuda da IA para questão ID: {}", questionId, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Erro inesperado ao acessar a ajuda da IA. Tente novamente mais tarde."
            ));
        }
    }
    
    @GetMapping("/gameover")
    public String gameOver(HttpSession httpSession, Model model, @RequestParam(required = false) Integer prize) {
        Long sessionId = (Long) httpSession.getAttribute("sessionId");
        if (sessionId == null) {
            return "redirect:/";
        }
        // Recarregar sessão do banco para garantir dados atualizados
        GameSession session = gameService.getSession(sessionId);
        
        // Se o prêmio foi passado como parâmetro, usar ele (mais confiável)
        if (prize != null && prize > 0) {
            session.setScore(prize);
            System.out.println("GameOver - Usando prêmio do parâmetro: R$ " + prize);
        }
        
        // Debug: verificar score
        System.out.println("GameOver - Session ID: " + sessionId);
        System.out.println("GameOver - Current Level: " + session.getCurrentLevel());
        System.out.println("GameOver - Score do banco: " + session.getScore());
        System.out.println("GameOver - Prêmio do parâmetro: " + prize);
        System.out.println("GameOver - Status: " + session.getStatus());
        
        model.addAttribute("session", session);
        model.addAttribute("finalPrize", session.getScore());
        return "gameover";
    }
    
    @GetMapping("/winner")
    public String winner(HttpSession httpSession, Model model) {
        Long sessionId = (Long) httpSession.getAttribute("sessionId");
        if (sessionId == null) {
            return "redirect:/";
        }
        GameSession session = gameService.getSession(sessionId);
        model.addAttribute("session", session);
        model.addAttribute("player", session.getPlayer());
        return "winner";
    }
}

