package com.example.classicwave.domain;

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
public class Quiz {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition="LONGTEXT")
    private String question;

    @ElementCollection
    @Builder.Default
    private List<String> optionList = new ArrayList<>();

    private int answer;

    @Column(columnDefinition="LONGTEXT")
    private String comment;
    @ManyToOne
    private QuizList quizList;

    private int submitCount;
    private int correctCount;

    public double calculateAccuracy() {
        if (submitCount == 0) {
            return 0.0; // 제출이 없는 경우
        }
        return (double) correctCount / submitCount; // 정답률 계산
    }
}
