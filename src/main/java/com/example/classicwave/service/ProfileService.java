package com.example.classicwave.service;


import com.example.classicwave.domain.Member;
import com.example.classicwave.dto.domain.MemberDto;
import com.example.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberDto getProfile(Long UserId){
        Member member = memberRepository.findById(UserId)
                .orElseThrow(()-> new RuntimeException("잘못된 요청 입니다."));

        return new MemberDto(member.getName(),member.getIntroduction());
    }

    @Transactional
    public MemberDto updateProfile(Long userId, String username, String introduction) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("잘못된 요청입니다."));

        if (username != null && !username.isEmpty()) {
            member.setName(username);
        }
        if (introduction != null) {
            member.setIntroduction(introduction);
        }

        memberRepository.save(member);

        return new MemberDto(member.getName(), member.getIntroduction());
    }
}
