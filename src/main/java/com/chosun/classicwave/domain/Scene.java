package com.chosun.classicwave.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

