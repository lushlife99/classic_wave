package com.example.classicwave.repository;

import com.example.classicwave.domain.Quiz;
import com.example.classicwave.domain.QuizList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByQuizList(QuizList quizList);
}
