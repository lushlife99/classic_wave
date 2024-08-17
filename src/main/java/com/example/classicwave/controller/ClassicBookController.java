package com.example.classicwave.controller;

import com.example.classicwave.service.BookService;
import com.example.classicwave.service.CartoonCreationService;

import com.example.classicwave.dto.request.EBookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classic-book")
@RequiredArgsConstructor
public class ClassicBookController {

    private final BookService bookService;
    private final CartoonCreationService cartoonCreationService;

    @PostMapping
    public ResponseEntity createClassicBook(@RequestBody EBookRequest bookRequest) {
        bookService.postToScheduler(bookRequest);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/test")
//    public SceneListResponse test(@RequestParam String title, @RequestParam String isbnId) {
//        return cartoonCreationService.getSceneListByBookInfo(title, isbnId);
//    }
}
