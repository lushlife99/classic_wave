package com.example.classicwave.controller;

import com.example.classicwave.domain.Member;
import com.example.classicwave.dto.domain.MemberDto;
import com.example.classicwave.repository.MemberRepository;
import com.example.classicwave.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "프로필", description = "조회 및 수정")
@RequestMapping("api/profile")
@RequiredArgsConstructor
@RestController
public class ProfileController {

    private final ProfileService profileService;
    private final MemberRepository memberRepository;

    @GetMapping
    @Operation(summary = "유저 프로필 조회", description = "로그인한 사용자의 프로필 정보를 조회.")
    public ResponseEntity<MemberDto> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String logInId = authentication.getName();

        Member member = memberRepository.findByLogInId(logInId)
                .orElseThrow(() -> new RuntimeException("잘못된 사용자 요청입니다."));

        // 로그인 ID로 프로필 조회
        MemberDto memberDto = profileService.getProfile(member.getId());
        return ResponseEntity.ok(memberDto);
    }


    @PatchMapping
    @Operation(summary = "유저 프로필 업데이트", description = "로그인한 사용자의 프로필 정보를 업데이트합니다. 사용자는 이름, 소개, 프로필 사진 등을 변경할 수 있습니다.")
    public ResponseEntity<MemberDto> updateUserProfile(@RequestBody MemberDto memberDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String logInId = authentication.getName();

        Member member = memberRepository.findByLogInId(logInId)
                .orElseThrow(() -> new RuntimeException("잘못된 사용자 요청입니다."));
        MemberDto updatedProfile = profileService.updateProfile(member.getId(), memberDto.getName(), memberDto.getIntroduction());

        return ResponseEntity.ok(updatedProfile);
    }
}
