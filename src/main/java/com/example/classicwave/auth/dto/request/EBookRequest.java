package com.example.classicwave.auth.dto.request;

import lombok.Data;

@Data
public class EBookRequest {

    private final String isbnId;
    private final String name;
}