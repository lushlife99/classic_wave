package com.chosun.classicwave.repository;

import com.chosun.classicwave.entity.QuizSubmit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSubmitRepository extends JpaRepository<QuizSubmit, Long> {
    List<QuizSubmit> findByMemberId(Long memberId);
}
