package com.example.classicwave.service;

import com.example.classicwave.domain.Book;
import com.example.classicwave.dto.domain.BookDto;
import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.enums.SearchCond;
import com.example.classicwave.error.CustomException;
import com.example.classicwave.error.ErrorCode;
import com.example.classicwave.openFeign.naver.NaverBookClient;
import com.example.classicwave.openFeign.naver.response.NaverBookResponse;
import com.example.classicwave.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final RedisTemplate<String, EBookRequest> redisTemplate;
    private final NaverBookClient naverBookClient;
    private final static String EBOOK_REQUEST_PREFIX = "ebookRequest";
    public void postToScheduler(EBookRequest bookRequest) {
        String key = EBOOK_REQUEST_PREFIX + ":" + bookRequest.getIsbnId();

        if(redisTemplate.hasKey(key))
            throw new CustomException(ErrorCode.ALREADY_POSTED_CLASSIC);

        redisTemplate.opsForSet().add(key, bookRequest);
    }

    public BookDto getBookMetadata(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        return new BookDto(book);
    }

    @Transactional
    public Book saveBook(EBookRequest bookRequest) {
        Book book = bookRequest.toEntity();
        return bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public Optional<BookDto> search(String searchText) {
        Optional<Book> optionalBook = bookRepository.findByName(searchText);

        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            return Optional.of(new BookDto(book));
        }

        return Optional.empty();
    }


    public NaverBookResponse searchToNaver(String searchText) {
        // encode query to utf-8
        String encodedSearchText;
        try {
            encodedSearchText = URLEncoder.encode(searchText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return naverBookClient.searchBooks(encodedSearchText, 1);
    }

    /**
     *  ToDo
     *
     *  페이징 쿼리최적화
     *  DTO에 바로 매핑
     */
    @Transactional(readOnly = true)
    public Page<BookDto> searchBookList(SearchCond searchCond, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        Page<Book> books;
        if (searchCond == SearchCond.popular) {
            books = bookRepository.findAllByOrderByLikesDesc(pageable);
        } else {
            books = bookRepository.findAllByOrderByCreatedTimeDesc(pageable);
        }

        List<BookDto> bookDtoList = books.getContent().stream()
                .map(BookDto::new)
                .toList();

        return new PageImpl<>(bookDtoList, pageable, books.getTotalElements());
    }

}