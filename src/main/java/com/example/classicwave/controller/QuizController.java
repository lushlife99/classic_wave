package com.example.classicwave.controller;

import com.example.classicwave.dto.request.QuizRequest;
import com.example.classicwave.dto.request.QuizSubmitRequest;
import com.example.classicwave.dto.response.QuizListResponse;
import com.example.classicwave.dto.response.QuizSubmitResponse;
import com.example.classicwave.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;


    @PostMapping("/create")
    public ResponseEntity<QuizListResponse> createAndSaveQuiz(@RequestBody QuizRequest quizRequest) {
            String bookTitle = quizRequest.getBookTitle();
            String isbnId = quizRequest.getIsbnId();

            QuizListResponse quizResponse = quizService.createAndSaveQuiz(bookTitle, isbnId);
            return ResponseEntity.ok(quizResponse);

    }


    @PostMapping("/submit")
    public ResponseEntity<QuizSubmitResponse> submitQuiz(@RequestBody QuizSubmitRequest quizSubmitRequest){
        Long quizListId = quizSubmitRequest.getQuizListId();
        List<Integer> userAnswers = quizSubmitRequest.getUserAnswers();
        QuizSubmitResponse quizSubmitResponse = quizService.submitQuiz(quizListId,userAnswers);

        return ResponseEntity.ok(quizSubmitResponse);
    }

}
