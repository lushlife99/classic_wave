package com.chosun.classicwave.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorName;
    @Column(unique = true)
    private String name;
    @NotNull
    private String folderName;

    private int publishedYear;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @OrderColumn(name = "scene_order")
    private List<Scene> sceneList = new ArrayList<>();

    private LocalDateTime createdTime;

    @Builder
    public Book(String authorName, String name, String folderName, int publishedYear, List<Scene> sceneList) {
        this.authorName = authorName;
        this.name = name;
        this.folderName = folderName != null ? folderName : UUID.randomUUID().toString();
        this.publishedYear = publishedYear;
        this.sceneList = sceneList != null ? sceneList : new ArrayList<>();
        this.createdTime = LocalDateTime.now();
    }

}