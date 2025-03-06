package com.chosun.classicwave.controller;

import com.chosun.classicwave.enums.Constant;
import com.chosun.classicwave.jwt.TokenInfo;
import com.chosun.classicwave.service.AuthService;
import com.chosun.classicwave.dto.request.AuthRequest;
import com.chosun.classicwave.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "유저 인증, 인가, 토큰재발급 API")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-expiration-time}")
    private int refreshExpirationTime;

    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "유저 회원가입")
    public ResponseEntity<Void> join(@RequestBody AuthRequest authRequest) {
        authService.join(authRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "id와 password로 로그인")
    public TokenInfo login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        TokenInfo tokenInfo = authService.login(authRequest);
        CookieUtil.addCookie(response, "refreshToken", tokenInfo.getRefreshToken(), refreshExpirationTime);
        return tokenInfo;
    }

    @GetMapping("/reissue/token")
    @Operation(summary = "jwt 재발급", description = "jwt - access, refresh 토큰 재발급")
    public TokenInfo reIssueJwt(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookieValue(request.getCookies(), Constant.REFRESH_TOKEN_COOKIE_VALUE.getValue());
        TokenInfo tokenInfo = authService.reIssueToken(refreshToken);
        CookieUtil.addCookie(response, "refreshToken", tokenInfo.getRefreshToken(), refreshExpirationTime);
        return tokenInfo;
    }
}
