package com.example.classicwave.openFeign.naver;

import com.example.classicwave.openFeign.naver.response.NaverBookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "naver-book-serach-service",
        url = "https://openapi.naver.com",
        configuration = NaverBookConfig.class
)
public interface NaverBookClient {
    @GetMapping("/v1/search/book.json")
    NaverBookResponse searchBooks(@RequestParam("query") String query, @RequestParam(value = "display", required = false) Integer display);

}