package com.example.classicwave.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookHistoryResponse {

    private String bookTitle;
    private String author;
    private String publishedYear;
    private Long quizsubmitId;
}
