package com.example.classicwave.service;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Member;
import com.example.classicwave.dto.request.BookCreateRequest;
import com.example.classicwave.dto.domain.BookDto;
import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.dto.response.BookCreateResponse;
import com.example.classicwave.enums.SearchCond;
import com.example.classicwave.error.CustomException;
import com.example.classicwave.error.ErrorCode;
import com.example.classicwave.openFeign.naver.NaverBookClient;
import com.example.classicwave.openFeign.naver.response.NaverBookResponse;
import com.example.classicwave.openFeign.stabilityai.StabilityAiClient;
import com.example.classicwave.openFeign.stabilityai.request.ImageRequest;
import com.example.classicwave.repository.BookRepository;
import com.example.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
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
import java.util.*;

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
    private final static String MEMBER_LIKE_KEY = "like:member"; // 유저가 관심 등록 한 책의 id list를 찾는 key
    private final static int PAGE_SIZE = 10;

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
            System.out.println(new BookDto(book));
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
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return naverBookClient.searchBooks(encodedSearchText, 1);
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
    @Cacheable(value = "books", key = "#searchCond + '_' + #page", condition = "#page == 0")
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
        String redisKey = MEMBER_LIKE_KEY;

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


    /**
     * Test method
     * @param size
     */
    public void createTestBookList(int size) {

        List<Book> bookList = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            Book book = Book.builder()
                    .name("test book title" + i)
                    .isbnId("test book isbnid" + i)
                    .authorName("test book author" + i)
                    .folderName(UUID.randomUUID().toString())
                    .sceneList(new ArrayList<>())
                    .build();

            bookList.add(book);
        }

        List<Book> books = bookRepository.saveAll(bookList);

        for (Book book : books) {
            redisTemplate.opsForZSet().add(SORTED_TOTAL_LIKES_KEY, book.getId().toString(), 1);

        }
    }

    //책 정보를 직접 입력해 추가
    public BookCreateResponse createBookDirect(BookCreateRequest request){

        Book book = Book.builder()
                .name(request.getBookName())
                .isbnId(request.getIsbn_id())
                .authorName(request.getAuthor_name())
                .folderName(request.getFolder_name())
                .build();

        Book saveBook = bookRepository.save(book);

        BookCreateResponse response = new BookCreateResponse(
                saveBook.getName(),
                saveBook.getIsbnId(),
                saveBook.getCreatedTime(),
                saveBook.getAuthorName(),
                saveBook.getFolderName()
        );

        return response;
    }

    //test


}