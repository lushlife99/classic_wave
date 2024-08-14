package com.example.classicwave.controller;

import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.service.ClassicBookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
