package com.example.classicwave.dto.request;

import lombok.Data;

@Data
public class EBookRequest {

    private final String isbnId;
    private final String name;
}