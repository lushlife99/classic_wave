package com.example.classicwave.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizHistoryResponse {
    private String question;
    private List<Integer> submitAnswerList;
    private int answer;
    private int score;
    private String comment;
}
