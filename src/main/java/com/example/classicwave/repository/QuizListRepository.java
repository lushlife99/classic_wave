package com.example.classicwave.repository;

import com.example.classicwave.domain.QuizList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizListRepository extends JpaRepository<QuizList, Long> {
}
