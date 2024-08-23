package com.example.classicwave.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
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
