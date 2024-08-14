package com.example.classicwave.quiz.dto;


import lombok.Data;

@Data
public class QuizRequest {
    private String bookTitle;
    private String isbnId;
}
