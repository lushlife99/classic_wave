package com.chosun.classicwave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizList {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Book book;

    @OneToMany(mappedBy = "quizList", cascade = CascadeType.ALL)
    private List<Quiz> quizzes;

    @OneToMany(mappedBy = "quizList", cascade = CascadeType.ALL)
    private List<QuizSubmit> submitList;

    @Builder
    public QuizList(Book book, List<Quiz> quizzes, List<QuizSubmit> submitList) {
        this.book = book;
        this.quizzes = quizzes == null ? new ArrayList<Quiz>() : quizzes;
        this.submitList = submitList == null ? new ArrayList<>() : submitList;
    }

    public static QuizList from(Book book) {
        return QuizList.builder()
                .book(book)
                .build();
    }

}
