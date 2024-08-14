package com.example.classicwave.auth.domain;

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
public class QuizSubmit {

    @Id @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    private QuizList quizList;

    @ElementCollection(fetch = FetchType.LAZY) @Builder.Default
    private List<Integer> submitAnswerList = new ArrayList<>(4);
    private double score;
}
