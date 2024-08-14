package com.example.classicwave.auth.controller;

import com.example.classicwave.auth.service.ClassicBookService;
import com.example.classicwave.auth.dto.request.EBookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classic-book")
@RequiredArgsConstructor
public class ClassicBookController {

    private final ClassicBookService classicBookService;

    @PostMapping
    public ResponseEntity createClassicBook(@RequestBody EBookRequest bookRequest) {
        classicBookService.postToScheduler(bookRequest);
        return ResponseEntity.ok().build();
    }
}
