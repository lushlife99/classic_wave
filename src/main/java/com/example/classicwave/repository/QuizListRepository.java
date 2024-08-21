package com.example.classicwave.repository;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.QuizList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizListRepository extends JpaRepository<QuizList, Long> {

    Optional<QuizList> findByBook(Book book);
}
