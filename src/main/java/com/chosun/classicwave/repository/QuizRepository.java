package com.chosun.classicwave.repository;

import com.chosun.classicwave.domain.Quiz;
import com.chosun.classicwave.domain.QuizList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByQuizList(QuizList quizList);
}
