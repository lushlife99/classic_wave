package com.chosun.classicwave.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BookCreateRequest {
    private String bookName;
    private String isbn_id;
    //private int likes;
    private String author_name;
    private String folder_name;



}
