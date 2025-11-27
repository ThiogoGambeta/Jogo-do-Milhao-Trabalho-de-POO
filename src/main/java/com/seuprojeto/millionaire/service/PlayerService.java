package com.seuprojeto.millionaire.service;

import com.seuprojeto.millionaire.model.Player;
import com.seuprojeto.millionaire.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    
    @Transactional
    public Player findOrCreatePlayer(String nickname) {
        return playerRepository.findByNickname(nickname)
            .orElseGet(() -> {
                Player newPlayer = Player.builder()
                    .nickname(nickname.trim())
                    .build();
                return playerRepository.save(newPlayer);
            });
    }
    
    public Player findById(Long id) {
        return playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Jogador não encontrado"));
    }
}

