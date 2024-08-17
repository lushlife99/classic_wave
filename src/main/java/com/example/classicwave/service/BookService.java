package com.example.classicwave.service;

import com.example.classicwave.domain.Book;
import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.error.CustomException;
import com.example.classicwave.error.ErrorCode;
import com.example.classicwave.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final RedisTemplate<String, EBookRequest> redisTemplate;
    private final static String EBOOK_REQUEST_PREFIX = "ebookRequest";
    public void postToScheduler(EBookRequest bookRequest) {
        String key = EBOOK_REQUEST_PREFIX + ":" + bookRequest.getIsbnId();

        if(redisTemplate.hasKey(key))
            throw new CustomException(ErrorCode.ALREADY_POSTED_CLASSIC);

        redisTemplate.opsForSet().add(key, bookRequest);
    }

    @Transactional
    public Book saveBook(EBookRequest bookRequest) {
        Book book = bookRequest.toEntity();
        return bookRepository.save(book);
    }
}
