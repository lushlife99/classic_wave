package com.example.classicwave.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizHistoryResponse {
    private String question;
    private List<Integer> submitAnswer; // 단일 제출 답변 리스트
    private int answer;
    private String comment;
}
