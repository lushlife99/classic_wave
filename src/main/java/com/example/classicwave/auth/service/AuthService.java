package com.example.classicwave.auth.service;

import com.example.classicwave.auth.domain.Member;
import com.example.classicwave.auth.dto.request.AuthRequest;
import com.example.classicwave.auth.error.CustomException;
import com.example.classicwave.auth.error.ErrorCode;
import com.example.classicwave.auth.jwt.JwtTokenProvider;
import com.example.classicwave.auth.jwt.TokenInfo;
import com.example.classicwave.auth.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        Member joinMember = Member.builder().logInId(joinRequest.getLoginId())
                .roles(Collections.singletonList("ROLE_USER")).password(encoder.encode(joinRequest.getPassword())).build();
        memberRepository.save(joinMember);
    }

    public TokenInfo login(AuthRequest loginRequest, HttpServletResponse response) {

        Member member = memberRepository.findByLogInId(loginRequest.getLoginId()).orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
        if(!encoder.matches(loginRequest.getPassword(), member.getPassword())){
            throw new CustomException(ErrorCode.MISMATCHED_PASSWORD);
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(), loginRequest.getPassword());
        Authentication authentication = authenticationManagerBuilder.authenticate(authenticationToken);
        return jwtTokenProvider.generateToken(authentication, response);
    }

    public TokenInfo reIssueToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = "";

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken"))
                refreshToken = cookie.getValue();
        }
        return jwtTokenProvider.reissueToken(refreshToken, response);
    }
}
