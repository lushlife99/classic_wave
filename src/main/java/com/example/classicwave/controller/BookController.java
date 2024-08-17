package com.example.classicwave.controller;

import com.example.classicwave.dto.domain.BookDto;
import com.example.classicwave.service.BookService;

import com.example.classicwave.dto.request.EBookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity createClassicBook(@RequestBody EBookRequest bookRequest) {
        bookService.postToScheduler(bookRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/metadata")
    public BookDto getMetadata(@RequestParam Long bookId) {
        return bookService.getBookMetadata(bookId);
    }
}
