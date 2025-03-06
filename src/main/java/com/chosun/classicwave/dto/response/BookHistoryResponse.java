package com.chosun.classicwave.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookHistoryResponse {

    private Long bookId;
    private String bookTitle;
    private String author;
    private int publishedYear;
    private Long quizSubmitId;
}
