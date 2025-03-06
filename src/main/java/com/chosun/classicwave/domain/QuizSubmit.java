package com.chosun.classicwave.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuizSubmit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    private QuizList quizList;

    @ElementCollection(fetch = FetchType.LAZY) @Builder.Default
    private List<Integer> submitAnswerList = new ArrayList<>(4);
    private int score;
}
