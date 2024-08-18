package com.example.classicwave.controller;

import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.openFeign.gutenberg.response.BookSearchResponse;
import com.example.classicwave.openFeign.gutenberg.GutenbergApiClient;
import com.example.classicwave.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final GutenbergApiClient gutenbergApiClient;
    private final BookService bookService;

    @GetMapping
    public BookSearchResponse test(@RequestParam String search, @RequestParam String copyright) {
        return gutenbergApiClient.searchBooks(search, copyright);
    }

    @PostMapping("/book-create")
    public ResponseEntity createClassicBook(@RequestBody EBookRequest bookRequest) {
        bookService.postToScheduler(bookRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/book")
    public ResponseEntity createTestingBook(@RequestBody EBookRequest bookRequest) {
        bookService.createTestBook(bookRequest);
        return ResponseEntity.ok().build();
    }


}
