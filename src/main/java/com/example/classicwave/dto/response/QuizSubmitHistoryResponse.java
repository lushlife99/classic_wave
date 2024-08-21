package com.example.classicwave.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizSubmitHistoryResponse {
    private List<QuizHistoryResponse> quizHistoryResponses;
    private int totalScore;
}
