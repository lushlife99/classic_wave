package com.example.classicwave.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EBookRequest {

    private String isbnId;
    private String authorName;
    private String name;
}