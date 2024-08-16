package com.example.classicwave.config;

import com.example.classicwave.domain.Book;
import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.dto.response.SceneListResponse;
import com.example.classicwave.repository.BookRepository;
import com.example.classicwave.service.CartoonCreationService;
import com.example.classicwave.service.S3FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final CartoonCreationService creationService;
    private final RedisTemplate<String, EBookRequest> redisTemplate;
    private final BookRepository bookRepository;
    private final S3FileUploadService s3Service;
    private final static String ebookRequestKeyPrefix = "ebookRequest:*";



    /**
     * 테스트 이후에 Delay 변경
     */

    @Scheduled(fixedDelay = 60000)
    public void createCartoon() {
        Set<String> keys = redisTemplate.keys(ebookRequestKeyPrefix);
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                EBookRequest bookRequest = redisTemplate.opsForSet().pop(key);
                Optional<Book> optionalBook = bookRepository.findByName(bookRequest.getName());

                if (optionalBook.isEmpty()) {

                    Book book = Book.builder()
                            .authorName(bookRequest.getAuthorName())
                            .likes(0L)
                            .name(bookRequest.getName())
                            .isbnId(bookRequest.getIsbnId())
                            .build();

                    SceneListResponse sceneListResponse = creationService.getSceneListByBookInfo(book);
                    List<Speech> speeches = creationService.generateAudios(sceneListResponse);
                    List<ImageGeneration> images = creationService.generateImages(sceneListResponse);

                    s3Service.uploadAudios(speeches, "test-audio");
                    s3Service.uploadImages(images, "test-images");
                    /**
                     * ToDo
                     *
                     * Entity 저장.
                     */
                }
            }
        } else {
            log.info("No ebook requests found.");
        }
    }
}
