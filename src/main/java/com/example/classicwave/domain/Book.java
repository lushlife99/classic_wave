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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorName;
    @Column(unique = true)
    private String name;
    @NotNull
    private String folderName;

    //발행 년도 추가
    private int publishedYear;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    @OrderColumn(name = "scene_order")
    @Builder.Default
    private List<Scene> sceneList = new ArrayList<>();

    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
    }
}