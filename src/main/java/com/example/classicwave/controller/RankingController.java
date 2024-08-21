package com.example.classicwave.controller;

import com.example.classicwave.dto.domain.RankDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "랭킹", description = "사용자들의 랭킹 조회")
@RequestMapping("api/ranking")
@RequiredArgsConstructor
@RestController
public class RankingController {

    @GetMapping
    @Operation(summary = "랭킹 정보 반환", description = "현재 사용자 및 전체 사용자에 대한 랭킹 정보를 반환합니다.")
    public ResponseEntity<RankDto> getRanking(){
        return null;
    }
}

