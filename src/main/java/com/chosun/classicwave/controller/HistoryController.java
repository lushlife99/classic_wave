package com.chosun.classicwave.controller;

import com.chosun.classicwave.entity.Member;
import com.chosun.classicwave.dto.response.BookHistoryResponse;
import com.chosun.classicwave.dto.response.QuizSubmitHistoryResponse;
import com.chosun.classicwave.repository.MemberRepository;
import com.chosun.classicwave.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "유저 히스토리", description = "문제를 풀었던 책 목록, 그리고 제출내역을 조회, 삭제")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;
    private final MemberRepository memberRepository;

    @GetMapping
    @Operation(summary = "책 리스트 반환", description = "퀴즈를 풀고 제출했던 책 리스트 반환")
    public ResponseEntity<List<BookHistoryResponse>> getUserBookHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String logInId = authentication.getName();

        Member member = memberRepository.findByLogInId(logInId)
                .orElseThrow(() -> new RuntimeException("잘못된 사용자 요청입니다."));

        List<BookHistoryResponse> bookHistories = historyService.getUserBookHistory(member.getId());

        return ResponseEntity.ok(bookHistories);
    }

    @GetMapping("/{summitId}")
    @Operation(summary = "제출 내역 반환", description = "책 리스트 조회에서 반환된 summitid로 제출된 퀴즈 내역과 관련 정보 반환")
    public ResponseEntity<QuizSubmitHistoryResponse> getQuizSubmitHistory(@PathVariable Long summitId) {
        QuizSubmitHistoryResponse quizHistory = historyService.getQuizSubmitHistory(summitId);

        return ResponseEntity.ok(quizHistory);
    }

    @DeleteMapping
    @Operation(summary = "히스토리 내역 삭제", description = "히스토리 내역을 지정해 삭제")
    public ResponseEntity<String> deleteQuizSubmitHistory(){
        return ResponseEntity.ok("퀴즈 삭제가 완료되었습니다.");
    }
}




