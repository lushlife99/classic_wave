package com.chosun.classicwave.controller;

import com.chosun.classicwave.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관심작품", description = "도서 관심작품 등록, 삭제, 내 관심작품 리스트 반환 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    @Operation(summary = "전자책 관심작품 등록")
    public List<Long> add(@RequestParam Long bookId, Authentication authentication) {
        likeService.likeBook(bookId, authentication);
        return likeService.getMemberLikeList(authentication);
    }

    @DeleteMapping
    @Operation(summary = "전자책 관심작품 삭제")
    public List<Long> delete(@RequestParam Long bookId, Authentication authentication) {
        likeService.unlikeBook(bookId, authentication);
        return likeService.getMemberLikeList(authentication);
    }

    @GetMapping
    @Operation(summary = "관심작품의 id 리스트 조회")
    public List<Long> getLikeList(Authentication authentication) {
        return likeService.getMemberLikeList(authentication);
    }
}
