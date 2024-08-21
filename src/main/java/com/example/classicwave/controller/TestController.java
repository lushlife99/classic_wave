package com.example.classicwave.controller;

import com.example.classicwave.dto.request.BookCreateRequest;
import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.dto.response.BookCreateResponse;
import com.example.classicwave.openFeign.gutenberg.response.BookSearchResponse;
import com.example.classicwave.openFeign.gutenberg.GutenbergApiClient;
import com.example.classicwave.service.BookService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@Hidden
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final GutenbergApiClient gutenbergApiClient;
    private final BookService bookService;

    @GetMapping
    public BookSearchResponse test(@RequestParam String search, @RequestParam String copyright) {
        return gutenbergApiClient.searchBooks(search, copyright);
    }

    @PostMapping("/create/book-list")
    public ResponseEntity createTestBookList() {
        bookService.createTestBookList(31);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/book-create")
    public ResponseEntity createClassicBook(@RequestBody EBookRequest bookRequest) {
        bookService.postToScheduler(bookRequest);
        return ResponseEntity.ok().build();
    }

    // 테스트용
    @GetMapping("/current-user")
    @Operation(summary = "현재 사용자 검증", description = "현재 로그인한 사용자의 정보를 표시하는 테스트 API 입니다.")
    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return "현재 사용자: " + authentication.getName();
        } else {
            return "인증되지 않은 사용자";
        }
    }

    @PostMapping("/create-book-direct")
    @Operation(summary = "책 정보 직접 추가", description = "책 이름, isbn_id, 작가 , 장르를 직접 지정해 책을 생성합니다.")
    public ResponseEntity<BookCreateResponse> createBookDirect (@RequestBody BookCreateRequest request){
        BookCreateResponse response = bookService.createBookDirect(request);
        return ResponseEntity.ok(response);
    }

}