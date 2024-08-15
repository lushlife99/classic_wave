package com.example.classicwave.dto.request;


import lombok.Data;

@Data
public class QuizRequest {
    private String bookTitle;
    private String isbnId;
}
