package com.example.classicwave.auth.repository;

import com.example.classicwave.auth.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
