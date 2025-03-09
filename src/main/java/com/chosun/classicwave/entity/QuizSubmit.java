package com.chosun.classicwave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class QuizSubmit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    private QuizList quizList;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> submitAnswerList = new ArrayList<>(4);
    private int score;
}
