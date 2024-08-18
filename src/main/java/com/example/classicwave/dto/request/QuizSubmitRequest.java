package com.example.classicwave.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class QuizSubmitRequest {
    private Long quizListId;
    private List<Integer> userAnswers;
}
