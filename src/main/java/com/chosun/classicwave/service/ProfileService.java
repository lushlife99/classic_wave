package com.chosun.classicwave.service;


import com.chosun.classicwave.entity.Member;
import com.chosun.classicwave.dto.domain.MemberDto;
import com.chosun.classicwave.error.CustomException;
import com.chosun.classicwave.error.ErrorCode;
import com.chosun.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final String FOLDER_NAME = "user";
    private final MemberRepository memberRepository;
    private final S3FileUploadService s3FileUploadService;

    public MemberDto getProfile(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        // 프로필 이미지 URL이 null이 아닐 경우 이미지를 가져옴
        String url = member.getImageName() != null
                ? s3FileUploadService.getImageUrl(FOLDER_NAME, member.getImageName())
                : null;

        return new MemberDto(member.getName(), member.getIntroduction(), url);
    }

    public MemberDto updateProfile(Long userId, String username, String introduction, Resource profileImage) throws IOException {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        if (username != null && !username.isEmpty()) {
            member.setName(username);
        }
        if (introduction != null) {
            member.setIntroduction(introduction);
        }

        if (profileImage != null && profileImage.contentLength() > 0) {
            String imageUrl = s3FileUploadService.uploadProfileImage(profileImage,userId);
            member.setImageName(imageUrl);
        }

        memberRepository.save(member);

        return new MemberDto(member.getName(), member.getIntroduction(), s3FileUploadService.getImageUrl(FOLDER_NAME, member.getImageName()));
    }
}
