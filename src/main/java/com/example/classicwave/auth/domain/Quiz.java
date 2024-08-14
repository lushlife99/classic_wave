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
public class Quiz {

    @Id @GeneratedValue
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
}
