package com.chosun.classicwave.domain;

import com.chosun.classicwave.dto.response.QuizListResponse;
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
    private List<String> optionList = new ArrayList<>();

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
        this.optionList = optionList;
        this.answer = answer;
        this.comment = comment;
        this.quizList = quizList;
    }

    public static Quiz from(QuizListResponse.QuestionResponse questionResponse, QuizList quizList, List<String> optionList, int answer) {
        return Quiz.builder()
                .question(questionResponse.question())
                .optionList(optionList)
                .answer(answer)
                .comment(questionResponse.comment())
                .quizList(quizList)
                .build();
    }

    public void plusSubmitCount() {
        this.submitCount++;
    }

    public double calculateAccuracy() {
        if (submitCount == 0) {
            return 0.0; // 제출이 없는 경우
        }
        return (double) correctCount / submitCount; // 정답률 계산
    }
}
