package com.example.classicwave.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id @GeneratedValue
    private Long id;

    private String isbnId;
    private String authorName;
    @Column(unique = true)
    private String name;
    private Long likes;

    @OneToMany(mappedBy = "book") @Builder.Default
    private List<Scene> sceneList = new ArrayList<>();

}