package com.chosun.classicwave.service;

import com.chosun.classicwave.domain.Member;
import com.chosun.classicwave.dto.domain.RankDto;
import com.chosun.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankService {

    private final MemberRepository memberRepository;

    public List<RankDto> getRanking() {
        List<Member> members = memberRepository.findAll();

        return members.stream()
                .sorted((m1, m2) -> Integer.compare(m2.getRating(), m1.getRating()))
                .map(member -> new RankDto(member.getLogInId(), member.getName(), member.getRating()))
                .collect(Collectors.toList());
    }
}
