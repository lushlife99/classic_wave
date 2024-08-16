package com.example.classicwave.controller;

import com.example.classicwave.dto.response.SceneListResponse;
import com.example.classicwave.service.CartoonCreationService;
import com.example.classicwave.service.ClassicBookService;
import com.example.classicwave.dto.request.EBookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.Generation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classic-book")
@RequiredArgsConstructor
public class ClassicBookController {

    private final ClassicBookService classicBookService;
    private final CartoonCreationService cartoonCreationService;

    @PostMapping
    public ResponseEntity createClassicBook(@RequestBody EBookRequest bookRequest) {
        classicBookService.postToScheduler(bookRequest);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/test")
//    public SceneListResponse test(@RequestParam String title, @RequestParam String isbnId) {
//        return cartoonCreationService.getSceneListByBookInfo(title, isbnId);
//    }
}
