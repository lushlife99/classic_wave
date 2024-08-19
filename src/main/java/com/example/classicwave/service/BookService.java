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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final RedisTemplate<String, EBookRequest> eBookRedisTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final NaverBookClient naverBookClient;
    private final static String EBOOK_REQUEST_PREFIX = "ebookRequest";
    private final static String SORTED_TOTAL_LIKES_KEY = "sorted:total_like";

    public void postToScheduler(EBookRequest bookRequest) {
        String key = EBOOK_REQUEST_PREFIX + ":" + bookRequest.getIsbnId();

        if(eBookRedisTemplate.hasKey(key))
            throw new CustomException(ErrorCode.ALREADY_POSTED_CLASSIC);

        eBookRedisTemplate.opsForSet().add(key, bookRequest);
    }

    @Transactional
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
        Optional<Book> optionalBook = bookRepository.findFirstByNameContaining(searchText);

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
     *  popular paging
     *  페이징 쿼리최적화
     *  DTO에 바로 매핑
     */
    @Transactional(readOnly = true)
    public Page<BookDto> searchBookList(SearchCond searchCond, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        if (searchCond == SearchCond.popular) {
            List<Long> bookIdList = getBookIdListOrderByLikes(page);
            List<Book> bookList = bookRepository.findAllById(bookIdList);
            List<BookDto> bookDtoList = bookList.stream()
                    .map(BookDto::new)
                    .toList();

            Long size = redisTemplate.opsForZSet().size(SORTED_TOTAL_LIKES_KEY);
            return new PageImpl<>(bookDtoList, pageable, size != null ? size : 0);
        }
        else {
            Page<Book> books = bookRepository.findAllByOrderByCreatedTimeDesc(pageable);
            List<BookDto> bookDtoList = books.getContent().stream()
                    .map(BookDto::new)
                    .toList();

            return new PageImpl<>(bookDtoList, pageable, books.getTotalElements());
        }
    }

    public List<Long> getBookIdListOrderByLikes(int page) {
        int pageSize = 10;
        int start = page * pageSize;
        int end = start + pageSize - 1;

        return redisTemplate.opsForZSet().range(SORTED_TOTAL_LIKES_KEY, start, end)
                .stream().map(Long::parseLong).toList();
    }



    public void createTestBookList(int size) {

        List<Book> bookList = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            Book book = Book.builder()
                    .name("test book title" + i)
                    .isbnId("test book isbnid" + i)
                    .authorName("test book author" + i)
                    .folderName(UUID.randomUUID().toString())
                    .build();

            bookList.add(book);
        }

        List<Book> books = bookRepository.saveAll(bookList);

        for (Book book : books) {
            redisTemplate.opsForZSet().add(SORTED_TOTAL_LIKES_KEY, book.getId().toString(), 1);

        }
    }

}