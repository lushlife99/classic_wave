package com.example.classicwave.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
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
    @NotNull
    private String folderName;

    //발행 년도 추가
    private String publishedYear;

    @OneToMany(mappedBy = "book")
    @OrderColumn(name = "scene_order")
    @Builder.Default
    private List<Scene> sceneList = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdTime;
}