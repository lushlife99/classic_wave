package com.chosun.classicwave.service;

import com.chosun.classicwave.entity.Book;
import com.chosun.classicwave.entity.Member;
import com.chosun.classicwave.dto.request.BookCreateRequest;
import com.chosun.classicwave.dto.domain.BookDto;
import com.chosun.classicwave.dto.request.EBookRequest;
import com.chosun.classicwave.dto.response.BookCreateResponse;
import com.chosun.classicwave.dto.response.PlotListResponse;
import com.chosun.classicwave.enums.SearchCond;
import com.chosun.classicwave.error.CustomException;
import com.chosun.classicwave.error.ErrorCode;
import com.chosun.classicwave.openFeign.naver.NaverBookClient;
import com.chosun.classicwave.openFeign.naver.response.NaverBookResponse;
import com.chosun.classicwave.openFeign.stabilityai.StabilityAiClient;
import com.chosun.classicwave.repository.BookRepository;
import com.chosun.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Todo
 * 검색 함수 (searchToNaver) 재사용성 생각해서 리팩토링
 */

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final StabilityAiClient stabilityAiClient;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, EBookRequest> eBookRedisTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final NaverBookClient naverBookClient;
    private final static String EBOOK_REQUEST_PREFIX = "ebookRequest";
    private final static String SORTED_TOTAL_LIKES_KEY = "sorted:total_like";
    private final static String MEMBER_LIKE_KEY_PREFIX = "like:member:"; // 유저가 관심 등록 한 책의 id list를 찾는 key
    private final static int SEARCH_RESULT_LIMIT = 10;
    private final static int PAGE_SIZE = 10;

    public void postToScheduler(EBookRequest bookRequest) {
        String key = EBOOK_REQUEST_PREFIX + ":" + bookRequest.getName();

        if(eBookRedisTemplate.hasKey(key))
            throw new CustomException(ErrorCode.ALREADY_POSTED_CLASSIC);

        eBookRedisTemplate.opsForSet().add(key, bookRequest);
    }

    @Transactional
    public BookDto getBookMetadata(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        return new BookDto(book);
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
        String encodedSearchText = URLEncoder.encode(searchText, StandardCharsets.UTF_8);
        return naverBookClient.searchBooks(encodedSearchText, SEARCH_RESULT_LIMIT);
    }

    /**
     * 캐싱
     * 한시간마다 인기순, 최신순 초기화
     *
     *  ToDo
     *
     *  popular paging
     *  페이징 쿼리최적화
     *  DTO에 바로 매핑
     */
    @Transactional(readOnly = true)
    public List<BookDto> searchBookList(Pageable pageable, SearchCond searchCond, int page) {

        if (searchCond == SearchCond.popular) {
            List<Long> bookIdList = getBookIdListOrderByLikes(page);
            List<Book> bookList = bookRepository.findAllById(bookIdList);
            return bookList.stream()
                    .map(BookDto::new)
                    .toList();
        }

        else {
            Page<Book> books = bookRepository.findAllByOrderByCreatedTimeDesc(pageable);
            return books.getContent().stream()
                    .map(BookDto::new)
                    .toList();
        }
    }

    public Long getTotalBookSize() {
        return redisTemplate.opsForZSet().size(SORTED_TOTAL_LIKES_KEY);
    }

    public List<Long> getBookIdListOrderByLikes(int page) {

        int start = page * PAGE_SIZE;
        int end = start + PAGE_SIZE - 1;

        return redisTemplate.opsForZSet().range(SORTED_TOTAL_LIKES_KEY, start, end)
                .stream().map(Long::parseLong).toList();
    }

    @Transactional
    public Page<BookDto> searchLikedBookList(int page, Authentication authentication) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Member member = memberRepository.findByLogInId(authentication.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        String redisKey = MEMBER_LIKE_KEY_PREFIX + member.getId();

        int start = page * PAGE_SIZE;
        int end = start + PAGE_SIZE - 1;

        List<Long> bookIdList = redisTemplate.opsForZSet().range(redisKey, start, end)
                .stream().map(Long::parseLong)
                .toList();

        Long size = redisTemplate.opsForZSet().size(redisKey);

        List<BookDto> bookDtoList = bookRepository.findAllById(bookIdList)
                .stream().map(BookDto::new)
                .toList();


        return new PageImpl<>(bookDtoList, pageable, size);
    }

}