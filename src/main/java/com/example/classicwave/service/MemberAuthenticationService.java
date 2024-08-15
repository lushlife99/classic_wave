package com.example.classicwave.service;


import com.example.classicwave.domain.Member;
import com.example.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAuthenticationService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        Member member =  memberRepository.findByLogInId(memberId)
                .orElseThrow(()-> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return createUserDetails(member);
    }

    private UserDetails createUserDetails(Member member) {

        return new org.springframework.security.core.userdetails.User(
                member.getLogInId(),
                member.getPassword(),
                member.getAuthorities()
        );
    }
}
