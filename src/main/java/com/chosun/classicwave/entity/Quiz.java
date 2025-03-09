package com.chosun.classicwave.entity;

import com.chosun.classicwave.dto.response.QuestionResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition="LONGTEXT")
    private String question;

    @ElementCollection
    private List<String> optionList;

    private int answer;

    @Column(columnDefinition="LONGTEXT")
    private String comment;
    @ManyToOne
    private QuizList quizList;

    private int submitCount;
    private int correctCount;

    @Builder
    public Quiz(QuizList quizList, String question, List<String> optionList, int answer, String comment) {
        this.question = question;
        this.optionList = optionList == null ? new ArrayList<>() : optionList;
        this.answer = answer;
        this.comment = comment;
        this.quizList = quizList;
    }


    public void plusSubmitCount() {
        this.submitCount++;
    }

    public void plusCorrectCount() {
        this.correctCount++;
    }

    public double calculateAccuracy() {
        if (submitCount == 0) {
            return 0.0; // 제출이 없는 경우
        }
        return (double) correctCount / submitCount; // 정답률 계산
    }
}
