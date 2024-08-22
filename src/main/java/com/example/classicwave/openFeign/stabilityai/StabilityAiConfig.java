package com.example.classicwave.openFeign.stabilityai;

import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StabilityAiConfig {

    @Value("${stabilityai.key}")
    private String key;

    @Bean
    public Request.Options options() {
        // 타임아웃 설정 (단위: 밀리초)
        return new Request.Options(
                5000, // 연결 타임아웃
                50000 // 읽기 타임아웃
        );
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("authorization", "Bearer "+ key);
            requestTemplate.header("accept", "image/*");
        };
    }
}