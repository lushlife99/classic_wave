package com.example.classicwave.dto.domain;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {

    private Long id;
    private String isbnId;
    private String authorName;
    private String name;
    private String thumbnailUrl;
    @Builder.Default
    private List<SceneDto> sceneList = new ArrayList<>();

    public BookDto(Book book) {
        this.id = book.getId();
        this.authorName = book.getAuthorName();
        this.name = book.getName();
        this.
        sceneList = new ArrayList<>();
        for (Scene scene : book.getSceneList()) {
            this.sceneList.add(new SceneDto(scene));
        }
    }

    public BookDto(String name){
        this.name = name;
    }
}
