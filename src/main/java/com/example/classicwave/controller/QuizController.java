package com.example.classicwave.controller;

import com.example.classicwave.dto.request.QuizRequest;
import com.example.classicwave.dto.response.QuizListResponse;
import com.example.classicwave.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/create")
    public ResponseEntity<QuizListResponse> createQuiz(@RequestBody QuizRequest quizRequest) {
        try {
            String bookTitle = quizRequest.getBookTitle();
            String isbnId = quizRequest.getIsbnId();

            QuizListResponse quizResponse = quizService.createQuiz(bookTitle, isbnId);
            return ResponseEntity.ok(quizResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            log.error("서버 오류 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
