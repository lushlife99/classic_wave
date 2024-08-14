package com.example.classicwave.auth.repository;

import com.example.classicwave.auth.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
