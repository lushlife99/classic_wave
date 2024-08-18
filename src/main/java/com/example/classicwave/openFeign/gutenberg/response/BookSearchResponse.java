package com.example.classicwave.openFeign.gutenberg.response;

import lombok.Data;

import java.util.List;

@Data
public class BookSearchResponse {
    private int count;
    private String next;
    private String previous;
    private List<BookResult> results;
}