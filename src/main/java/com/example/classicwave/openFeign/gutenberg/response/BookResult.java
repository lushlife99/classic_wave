package com.example.classicwave.openFeign.gutenberg.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BookResult {
    private int id;
    private String title;
    private List<Author> authors;
    private List<String> subjects = new ArrayList<>();
    private List<String> bookshelves = new ArrayList<>();
    private List<String> languages = new ArrayList<>();
    private boolean copyright;
    private String media_type;
    private Map<String, String> formats;
    private int download_count;
}