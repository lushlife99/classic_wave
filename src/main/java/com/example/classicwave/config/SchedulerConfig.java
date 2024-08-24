package com.example.classicwave.config;

import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.service.BookService;
import com.example.classicwave.service.CartoonCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final CartoonCreationService creationService;
    private final BookService bookService;
    private final RedisTemplate<String, EBookRequest> redisTemplate;
    private final static String EBOOK_REQUEST_PREFIX = "ebookRequest:*";

    @Scheduled(fixedDelay = 6000)
    public void runCreateCartoon() throws IOException {

        Set<String> keys = redisTemplate.keys(EBOOK_REQUEST_PREFIX);
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                creationService.createCartoon(key);
            }
        }
    }


}
