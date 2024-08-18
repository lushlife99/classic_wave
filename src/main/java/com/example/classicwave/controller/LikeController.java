package com.example.classicwave.controller;

import com.example.classicwave.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public List<Long> add(@RequestParam Long bookId, Authentication authentication) {
        likeService.likeBook(bookId, authentication);
        return likeService.getMemberLikeList(authentication);
    }

    @DeleteMapping
    public List<Long> delete(@RequestParam Long bookId, Authentication authentication) {
        likeService.unlikeBook(bookId, authentication);
        return likeService.getMemberLikeList(authentication);
    }

    @GetMapping
    public List<Long> getLikeList(Authentication authentication) {
        return likeService.getMemberLikeList(authentication);
    }
}
