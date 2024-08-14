package com.example.classicwave.auth.repository;

import com.example.classicwave.auth.domain.QuizSubmit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmitRepository extends JpaRepository<QuizSubmit, Long> {
}
