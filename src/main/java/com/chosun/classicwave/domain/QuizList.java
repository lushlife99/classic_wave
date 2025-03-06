package com.chosun.classicwave.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
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
    public QuizList(Book book) {
        this.book = book;
    }

    public static QuizList from(Book book) {
        return QuizList.builder()
                .book(book)
                .build();
    }

}
