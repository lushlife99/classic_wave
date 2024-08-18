package com.example.classicwave.controller;

import com.example.classicwave.dto.request.QuizRequest;
import com.example.classicwave.dto.request.QuizSubmitRequest;
import com.example.classicwave.dto.response.QuizListResponse;
import com.example.classicwave.dto.response.QuizSubmitResponse;
import com.example.classicwave.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "퀴즈", description = "퀴즈 생성 및 제출 API")
@Slf4j
@RestController
@RequestMapping("api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/create")
    @Operation(summary = "퀴즈 생성", description = "책 이름과 isbnid를 이용해 퀴즈를 생성")
    public ResponseEntity<QuizListResponse> createAndSaveQuiz(@RequestBody QuizRequest quizRequest) {
            String bookTitle = quizRequest.getBookTitle();
            String isbnId = quizRequest.getIsbnId();

            QuizListResponse quizResponse = quizService.createAndSaveQuiz(bookTitle, isbnId);
            return ResponseEntity.ok(quizResponse);

    }

    @PostMapping("/submit")
    @Operation(summary = "퀴즈 제출 및 채점", description = "사용자가 제출한 답을 바탕으로 답을 채점 후 점수를 반환")
    public ResponseEntity<QuizSubmitResponse> submitQuiz(@RequestBody QuizSubmitRequest quizSubmitRequest){
        Long quizListId = quizSubmitRequest.getQuizListId();
        List<Integer> userAnswers = quizSubmitRequest.getUserAnswers();
        QuizSubmitResponse quizSubmitResponse = quizService.submitQuiz(quizListId,userAnswers);

        return ResponseEntity.ok(quizSubmitResponse);
    }

}
