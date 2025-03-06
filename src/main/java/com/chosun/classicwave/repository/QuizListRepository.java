package com.chosun.classicwave.repository;

import com.chosun.classicwave.domain.Book;
import com.chosun.classicwave.domain.QuizList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizListRepository extends JpaRepository<QuizList, Long> {

    Optional<QuizList> findByBook(Book book);
}
