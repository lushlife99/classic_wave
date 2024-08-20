package com.example.classicwave.controller;

import com.example.classicwave.domain.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "프로필", description = "조회 및 수정")
@RequiredArgsConstructor
@RestController
public class ProfileController {

    @GetMapping
    @Operation(summary = "유저 프로필 반환", description = "로그인한 사용자의 프로필 정보를 반환합니다.")
    public ResponseEntity<Member> getUserProfile() {
        return null;
    }


    @PatchMapping
    @Operation(summary = "유저 프로필 업데이트", description = "로그인한 사용자의 프로필 정보를 업데이트합니다. 사용자는 이름, 소개, 프로필 사진 등을 변경할 수 있습니다.")
    public ResponseEntity<Member> updateUserProfile() {
        return null;
    }

}
