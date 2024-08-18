package com.example.classicwave.controller;

import com.example.classicwave.dto.domain.BookDto;
import com.example.classicwave.enums.SearchCond;
import com.example.classicwave.openFeign.naver.response.NaverBookResponse;
import com.example.classicwave.service.BookService;

import com.example.classicwave.dto.request.EBookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity createClassicBook(@RequestBody EBookRequest bookRequest) {
        bookService.postToScheduler(bookRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/metadata")
    public BookDto getMetadata(@RequestParam Long bookId) {
        return bookService.getBookMetadata(bookId);
    }

    @Operation(summary = "도서 검색", description = "검색어를 이용해 DB 또는 Naver API로부터 도서를 검색. " +
            "DB에 책이 존재하면 `BookDto`를 반환하고, 없으면 Naver API로부터 결과를 받아 `NaverBookResponse`를 반환",
            responses = {
                    @ApiResponse(
                            description = "DB에서 찾은 도서 정보",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BookDto.class
                                    )
                            )),
                    @ApiResponse(
                            description = "Naver API에서 찾은 도서 정보",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NaverBookResponse.class
                                    )
                            ))
            })
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String searchText) {
        Optional<BookDto> optionalBook = bookService.search(searchText);
        if (optionalBook.isPresent()) {
            return ResponseEntity.ok(optionalBook.get());
        }

        return ResponseEntity.ok(bookService.searchToNaver(searchText));
    }

    @GetMapping("/list/search")
    public Page<BookDto> searchBookList(
            @Parameter(description = "책 리스트를 페이징하여 반환. 검색 조건 (popular 또는 latest)",
                    schema = @Schema(implementation = SearchCond.class))
            @RequestParam SearchCond searchCond,
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication) {

        return bookService.searchBookList(searchCond, page);
    }

}