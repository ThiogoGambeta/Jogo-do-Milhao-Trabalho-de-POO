package com.seuprojeto.millionaire.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiResponseDTO {
    
    @JsonProperty("letter")
    private String letter; // "A", "B", "C" ou "D"
    
    @JsonProperty("answer")
    private String answer; // Texto da resposta
    
    @JsonProperty("confidence")
    private int confidence; // 1 a 100
    
    @JsonProperty("explanation")
    private String explanation; // Explicação curta
}

