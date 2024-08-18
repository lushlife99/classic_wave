package com.example.classicwave.repository;

import com.example.classicwave.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbnId(String isbnId);
}
