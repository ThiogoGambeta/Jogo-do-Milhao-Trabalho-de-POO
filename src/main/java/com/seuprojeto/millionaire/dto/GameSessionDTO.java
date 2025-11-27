package com.seuprojeto.millionaire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSessionDTO {
    private Long id;
    private String playerNickname;
    private int currentLevel;
    private int score;
    private int skipsLeft;
    private boolean fiftyFiftyUsed;
    private boolean aiHelpUsed;
}

