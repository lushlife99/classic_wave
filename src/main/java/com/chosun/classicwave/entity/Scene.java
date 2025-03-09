package com.chosun.classicwave.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Scene {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;
    private String photoId;
    @Column(columnDefinition="LONGTEXT")
    private String description;
    @Column(columnDefinition="LONGTEXT")
    private String plotSummary;

}

