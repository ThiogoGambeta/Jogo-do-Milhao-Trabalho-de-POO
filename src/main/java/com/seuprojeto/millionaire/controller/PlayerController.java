package com.seuprojeto.millionaire.controller;

import com.seuprojeto.millionaire.model.GameSession;
import com.seuprojeto.millionaire.model.Player;
import com.seuprojeto.millionaire.service.GameService;
import com.seuprojeto.millionaire.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/player")
@RequiredArgsConstructor
public class PlayerController {
    
    private final PlayerService playerService;
    private final GameService gameService;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerPlayer(
            @RequestParam @NotBlank @Size(min = 3, max = 50) String nickname,
            HttpSession session) {
        
        // Sanitização
        nickname = nickname.trim().replaceAll("[^a-zA-Z0-9áéíóúãõâêô ]", "");
        
        if (nickname.length() < 3) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Apelido deve ter pelo menos 3 caracteres"
            ));
        }
        
        try {
            Player player = playerService.findOrCreatePlayer(nickname);
            GameSession gameSession = gameService.startNewGame(player);
            
            session.setAttribute("sessionId", gameSession.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "redirectUrl", "/game"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erro ao criar jogador: " + e.getMessage()
            ));
        }
    }
}

