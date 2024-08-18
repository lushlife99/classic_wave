package com.example.classicwave.config;

import com.amazonaws.util.StringUtils;
import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.dto.response.SceneListResponse;
import com.example.classicwave.openFeign.gutenberg.response.BookSearchResponse;
import com.example.classicwave.openFeign.gutenberg.GutenbergApiClient;
import com.example.classicwave.openFeign.gutenberg.response.BookResult;
import com.example.classicwave.repository.BookRepository;
import com.example.classicwave.service.BookService;
import com.example.classicwave.service.CartoonCreationService;
import com.example.classicwave.service.S3FileUploadService;
import com.example.classicwave.service.SceneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final CartoonCreationService creationService;
    private final RedisTemplate<String, EBookRequest> redisTemplate;
    private final static String EBOOK_REQUEST_PREFIX = "ebookRequest:*";



    /**
     * ToDo
     *
     * 1. 테스트 이후에 Delay 변경
     * 2. 책이 완전히 생성될 때 Book Entity 생성되게 Transaction
     *
     *
     */



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
