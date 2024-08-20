package com.example.classicwave.controller;

import com.example.classicwave.jwt.TokenInfo;
import com.example.classicwave.service.AuthService;
import com.example.classicwave.dto.request.AuthRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "유저 인증, 인가, 토큰재발급 API")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "유저 회원가입")
    public ResponseEntity join(@RequestBody AuthRequest authRequest) {
        authService.join(authRequest);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "id와 password로 로그인")
    public TokenInfo login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        return authService.login(authRequest, response);
    }

    @GetMapping("/reIssueJwt")
    @Operation(summary = "jwt 재발급", description = "jwt - access, refresh 토큰 재발급")
    public TokenInfo reIssueJwt(HttpServletRequest request, HttpServletResponse response) {
        return authService.reIssueToken(request, response);
    }


}
