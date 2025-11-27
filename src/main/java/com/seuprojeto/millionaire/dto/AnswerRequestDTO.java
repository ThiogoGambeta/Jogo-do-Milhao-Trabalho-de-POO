package com.seuprojeto.millionaire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequestDTO {
    private Long questionId;
    private char answer;
    private int timeSpent;
    private String lifelineUsed;
}

