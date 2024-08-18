package com.example.classicwave.openFeign.naver;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NaverBookConfig {
    @Value("${naver.cloud.api.client-secret}")
    private String clientSecret;

    @Value("${naver.cloud.api.client-id}")
    private String clientId;


    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Naver-Client-Id", clientId);
            requestTemplate.header("X-Naver-Client-Secret", clientSecret);
        };
    }

}
