package com.example.classicwave.service;

import com.amazonaws.util.StringUtils;
import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.dto.response.SceneListResponse;
import com.example.classicwave.dto.response.SceneResponse;
import com.example.classicwave.openFeign.gutenberg.GutenbergApiClient;
import com.example.classicwave.openFeign.gutenberg.response.BookResult;
import com.example.classicwave.openFeign.gutenberg.response.BookSearchResponse;
import com.example.classicwave.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.StyleEnum;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartoonCreationService {

    @Value("classpath:/prompts/scene-generation-system-message.st")
    private Resource sceneSystemPrompt;
    @Value("classpath:/prompts/scene-generation-user-message.st")
    private Resource sceneUserPrompt;
    private final static String IMAGE_PREFIX = "/image";
    private final static String AUDIO_PREFIX = "/audio";

    private final BookRepository bookRepository;
    private final BookService bookService;
    private final S3FileUploadService s3Service;
    private final GutenbergApiClient gutenbergApiClient;
    private final SceneService sceneService;
    private final OpenAiAudioSpeechModel audioSpeechModel;
    private final StabilityAiImageModel imageModel;
    private final OpenAiChatModel openAiChatModel;
    private final RedisTemplate<String, EBookRequest> redisTemplate;

    @Transactional
    public void createCartoon(String key) throws IOException {
        EBookRequest bookRequest = redisTemplate.opsForSet().pop(key);
        Optional<Book> optionalBook = bookRepository.findByName(bookRequest.getName());

        if (optionalBook.isEmpty()) {

            // 1. Create Book entity
            Book book = bookService.saveBook(bookRequest);

            // 2. Create SceneList to asking GPT model
            SceneListResponse sceneListResponse = getSceneListByBookInfo(book);
            book.setAuthorName(sceneListResponse.author());

            // 3. Search copyright this book

            String urlEncodedQuery = URLEncoder.encode(sceneListResponse.bookTitle(), StandardCharsets.UTF_8)
                        .replace("+", "%20");

            BookSearchResponse bookSearchResponse = gutenbergApiClient.searchBooks(urlEncodedQuery, Boolean.toString(false));
            List<BookResult> bookResults = bookSearchResponse.getResults();
            for (BookResult searchResult : bookResults) {
                if (StringUtils.lowerCase(searchResult.getTitle()).contains(StringUtils.lowerCase(sceneListResponse.bookTitle()))) {

                    // 4. Generate images and audios
                    List<Scene> scenes = sceneService.saveSceneList(book, sceneListResponse);

                    List<Speech> speeches = generateAudios(sceneListResponse);
                    s3Service.uploadAudios(speeches, book.getFolderName() + AUDIO_PREFIX);

                    List<ImageGeneration> images = generateImages(sceneListResponse);
                    s3Service.uploadImages(images, book.getFolderName() + IMAGE_PREFIX);
                    book.setSceneList(scenes);
                    return;
                }
            }

        }
    }

    public SceneListResponse getSceneListByBookInfo(Book book) {

        BeanOutputConverter<SceneListResponse> outputConverter = new BeanOutputConverter<>(SceneListResponse.class);
        PromptTemplate userPrompt = getUserPrompt(book.getName(), book.getIsbnId(), outputConverter.getFormat());
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(sceneSystemPrompt);
        Prompt prompt = new Prompt(List.of(userPrompt.createMessage(), systemPrompt.createMessage()),
                OpenAiChatOptions.builder()
                        .withMaxTokens(4096)
                        .build());
        Generation result = openAiChatModel.call(prompt).getResult();
        return outputConverter.convert(result.getOutput().getContent());
    }

    public List<ImageGeneration> generateImages(SceneListResponse sceneListResponse) {
        List<String> prompts = sceneListResponse.sceneResponseList().stream()
                .map(SceneResponse::description)
                .toList();

        List<ImageGeneration> imageResults = new ArrayList<>();

        for (String prompt : prompts) {
            ImageResponse response = imageModel.call(
                    new ImagePrompt(
                            prompt,
                            StabilityAiImageOptions.builder()
                                    .withStylePreset(StyleEnum.COMIC_BOOK)
                                    .build())
            );
            imageResults.add(response.getResult());
        }

        return imageResults;
    }

    public List<Speech> generateAudios(SceneListResponse sceneListResponse) {
        List<String> prompts = sceneListResponse.sceneResponseList().stream()
                .map(SceneResponse::plotSummary)
                .toList();

        List<Speech> speechResult = new ArrayList<>();

        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .withResponseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .withSpeed(1.0f)
                .withModel(OpenAiAudioApi.TtsModel.TTS_1.value)
                .build();

        for (String prompt : prompts) {

            SpeechPrompt speechPrompt = new SpeechPrompt(prompt, speechOptions);
            SpeechResponse response = audioSpeechModel.call(speechPrompt);
            speechResult.add(response.getResult());
        }

        return speechResult;
    }

    private PromptTemplate getUserPrompt(String title, String isbnId, String format) {
        return new PromptTemplate(sceneUserPrompt, Map.of("title", title, "isbnId", isbnId, "format", format));
    }
}
