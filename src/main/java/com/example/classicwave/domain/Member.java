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
public class Member {

    @Id @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String userId;
    private String password;
    @OneToMany(mappedBy = "member")
    private List<QuizSubmit> quizSubmitList = new ArrayList<>();

}
