package com.example.classicwave.openFeign.gutenberg.response;

import lombok.Data;

@Data
public class Author {
    private String name;
    private Integer birth_year;
    private Integer death_year;
}
