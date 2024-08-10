package com.example.classicwave.service;

import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.error.CustomException;
import com.example.classicwave.error.ErrorCode;
import com.example.classicwave.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassicBookService {

    private final BookRepository bookRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final static String ebookRequestKey = "ebookRequest";
    public void postToScheduler(EBookRequest bookRequest) {
        String key = ebookRequestKey + ":" + bookRequest.getIsbnId();

        if(redisTemplate.hasKey(key))
            throw new CustomException(ErrorCode.ALREADY_POSTED_CLASSIC);

        redisTemplate.opsForSet().add(key, bookRequest.getName());
    }
}
