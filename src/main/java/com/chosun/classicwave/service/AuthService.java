package com.chosun.classicwave.service;

import com.chosun.classicwave.entity.Member;
import com.chosun.classicwave.dto.request.AuthRequest;
import com.chosun.classicwave.error.CustomException;
import com.chosun.classicwave.error.ErrorCode;
import com.chosun.classicwave.jwt.JwtTokenProvider;
import com.chosun.classicwave.jwt.TokenInfo;
import com.chosun.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    public void join(AuthRequest joinRequest) {

        Optional<Member> user= memberRepository.findByLogInId(joinRequest.getLoginId());
        if(user.isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_EXIST_MEMBER);
        }

        Member joinMember = Member.builder()
                .logInId(joinRequest.getLoginId())
                .roles(Collections.singletonList("ROLE_USER"))
                .name(joinRequest.getName())
                .password(encoder.encode(joinRequest.getPassword()))
                .build();
        memberRepository.save(joinMember);
    }

    public TokenInfo login(AuthRequest loginRequest) {

        Member member = memberRepository.findByLogInId(loginRequest.getLoginId()).orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
        if(!encoder.matches(loginRequest.getPassword(), member.getPassword())){
            throw new CustomException(ErrorCode.MISMATCHED_PASSWORD);
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(), loginRequest.getPassword());
        Authentication authentication = authenticationManagerBuilder.authenticate(authenticationToken);
        return jwtTokenProvider.generateToken(authentication);
    }

    public TokenInfo reIssueToken(String refreshToken) {

        return jwtTokenProvider.reissueToken(refreshToken);
    }
}
