package com.example.classicwave.controller;

import com.example.classicwave.domain.Member;
import com.example.classicwave.dto.domain.MemberDto;
import com.example.classicwave.repository.MemberRepository;
import com.example.classicwave.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "프로필", description = "프로필 조회 및 수정")
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@RestController
public class ProfileController {

    private final ProfileService profileService;
    private final MemberRepository memberRepository;

    @GetMapping
    @Operation(summary = "유저 프로필 조회", description = "사용자의 프로필 정보를 조회.")
    public MemberDto getUserProfile() throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String logInId = authentication.getName();

        Member member = memberRepository.findByLogInId(logInId)
                .orElseThrow(() -> new RuntimeException("잘못된 사용자 요청입니다."));

        // 로그인 ID로 프로필 조회
        MemberDto memberDto = profileService.getProfile(member.getId());
        return memberDto;
    }


    @PatchMapping
    @Operation(summary = "유저 프로필 업데이트", description = "사용자의 프로필 정보를 업데이트합니다.")
    public ResponseEntity<MemberDto> updateUserProfile(
            @RequestParam("name") String name,
            @RequestParam("introduction") String introduction,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String logInId = authentication.getName();

        Member member = memberRepository.findByLogInId(logInId)
                .orElseThrow(() -> new RuntimeException("잘못된 사용자 요청입니다."));

        Resource resource = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            byte[] bytes = profileImage.getBytes();
            resource = new ByteArrayResource(bytes);
        };

        MemberDto updatedProfile = profileService.updateProfile(member.getId(), name, introduction, resource);

        return ResponseEntity.ok(updatedProfile);
    }

}
