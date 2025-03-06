package com.chosun.classicwave.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuizList {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Book book;

    @OneToMany(mappedBy = "quizList", cascade = CascadeType.ALL)
    private List<Quiz> quizzes;

    @OneToMany(mappedBy = "quizList", cascade = CascadeType.ALL)
    private List<QuizSubmit> submitList;



}
