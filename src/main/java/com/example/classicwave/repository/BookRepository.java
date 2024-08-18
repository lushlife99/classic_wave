package com.example.classicwave.repository;

import com.example.classicwave.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbnId(String isbnId);
    Optional<Book> findByName(String name);
//    Page<Book> findAllByOrderByLikesDesc(Pageable pageable);
    Page<Book> findAllByOrderByCreatedTimeDesc(Pageable pageable);

}
