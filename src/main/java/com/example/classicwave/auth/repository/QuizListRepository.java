package com.example.classicwave.auth.repository;

import com.example.classicwave.auth.domain.QuizList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizListRepository extends JpaRepository<QuizList, Long> {
}
