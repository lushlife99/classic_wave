package com.example.classicwave.controller;

import com.example.classicwave.dto.domain.BookDto;
import com.example.classicwave.enums.SearchCond;
import com.example.classicwave.openFeign.naver.response.NaverBookResponse;
import com.example.classicwave.service.BookService;

import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.service.S3FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "책", description = "도서 생성, 검색, 정보 제공 API")
@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final S3FileUploadService s3FileUploadService;

    @PostMapping
    @Operation(summary = "전자책 등록 신청", description = "서버에 등록되지 않은 전자책을 생성하는 신청을 한다.")
    public ResponseEntity createClassicBook(@RequestBody EBookRequest bookRequest) {
        bookService.postToScheduler(bookRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/metadata")
    @Operation(summary = "전자책을 이루는 하위 장면들의 정보 조회", description = "전자책을 이루는 하위 장면들의 정보를 받아옴")
    public BookDto getMetadata(@RequestParam Long bookId) {
        return bookService.getBookMetadata(bookId);
    }

    @Operation(
            summary = "도서 검색", description = "검색어를 이용해 DB 또는 Naver API로부터 도서를 검색. " +
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
    @Operation(summary = "정렬된 전자책 리스트들을 조회", description = "책 리스트를 페이징하여 반환. 검색 조건 (popular 또는 latest)")
    public Page<BookDto> searchBookList(
            @Parameter(schema = @Schema(implementation = SearchCond.class))
            @RequestParam("searchCond") SearchCond searchCond,
            @RequestParam(defaultValue = "0", name = "page") int page) {

        Pageable pageable = PageRequest.of(page, 10);

        List<BookDto> bookDtos = bookService.searchBookList(pageable, searchCond, page);
        Long count = bookService.getTotalBookSize();
        return new PageImpl<>(bookDtos, pageable, count);
    }

    @GetMapping("/liked-list")
    @Operation(summary = "관심작품 리스트 조회", description = "최신순으로 정렬해서 관심작품 리스트 조회")
    public Page<BookDto> searchLikedBookList(@RequestParam(defaultValue = "0") int page, Authentication authentication) {
        return bookService.searchLikedBookList(page, authentication);
    }

    @GetMapping("/thumbnail")
    public String getBookThumbnailUrl(@RequestParam("bookId") long bookId) {
        return s3FileUploadService.getBookThumbnail(bookId);
    }

}