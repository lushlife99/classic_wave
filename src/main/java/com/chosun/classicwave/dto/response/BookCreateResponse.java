package com.chosun.classicwave.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookCreateResponse {
    private String bookName;
    private LocalDateTime createdTime;
    //private int likes;
    private String author_name;
    private String folder_name;

}