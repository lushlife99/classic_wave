package com.example.classicwave.repository;

import com.example.classicwave.domain.QuizSubmit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSubmitRepository extends JpaRepository<QuizSubmit, Long> {
    List<QuizSubmit> findByMemberId(Long memberId);
}
