package com.example.classicwave.service;


import com.example.classicwave.domain.Member;
import com.example.classicwave.dto.domain.MemberDto;
import com.example.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final S3FileUploadService s3FileUploadService;

    @Transactional
    public MemberDto getProfile(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("잘못된 요청입니다."));

        // 프로필 이미지 URL이 null이 아닐 경우 이미지를 가져옴
        Resource profileImageResource = member.getImagename() != null
                ? s3FileUploadService.getImage("user", member.getImagename())
                : null;

        return new MemberDto(member.getName(), member.getIntroduction(), profileImageResource);
    }

    @Transactional
    public MemberDto updateProfile(Long userId, String username, String introduction, Resource profileImage) throws IOException {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("잘못된 요청입니다."));

        if (username != null && !username.isEmpty()) {
            member.setName(username);
        }
        if (introduction != null) {
            member.setIntroduction(introduction);
        }

        if (profileImage != null && profileImage.contentLength() > 0) {
            String imageUrl = s3FileUploadService.uploadProfileImage(profileImage,userId);
            member.setImagename(imageUrl);
        }

        memberRepository.save(member);

        return new MemberDto(member.getName(), member.getIntroduction(),profileImage);
    }
}
