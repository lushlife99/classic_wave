package com.example.classicwave.service;

import com.amazonaws.util.StringUtils;
import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.dto.response.SceneListResponse;
import com.example.classicwave.dto.response.SceneResponse;
import com.example.classicwave.error.CustomException;
import com.example.classicwave.error.ErrorCode;
import com.example.classicwave.openFeign.gutenberg.GutenbergApiClient;
import com.example.classicwave.openFeign.gutenberg.response.BookResult;
import com.example.classicwave.openFeign.gutenberg.response.BookSearchResponse;
import com.example.classicwave.openFeign.stabilityai.StabilityAiClient;
import com.example.classicwave.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
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

import java.io.CharConversionException;
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
    @Value("classpath:/prompts/book-info-user-message.st")
    private Resource sceneUserPrompt;
    private final static String IMAGE_PREFIX = "/image";
    private final static String AUDIO_PREFIX = "/audio";

    private final StabilityAiClient stabilityAiClient;
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
            book.setPublishedYear(sceneListResponse.pubYear());

            System.out.println(sceneListResponse);

            if (sceneListResponse.copyRight()) {
                throw new CustomException(ErrorCode.COPYRIGHT_BOOK);
            }

            // 3. Create SceneList
            List<Scene> scenes = sceneService.saveSceneList(book, sceneListResponse);

            List<Resource> images = generateImages(sceneListResponse);
            s3Service.uploadImages(images, book.getFolderName() + IMAGE_PREFIX);

            List<Speech> speeches = generateAudios(sceneListResponse);
            s3Service.uploadAudios(speeches, book.getFolderName() + AUDIO_PREFIX);

            book.setSceneList(scenes);

        }
    }

    public SceneListResponse getSceneListByBookInfo(Book book) {

        BeanOutputConverter<SceneListResponse> outputConverter = new BeanOutputConverter<>(SceneListResponse.class);
        PromptTemplate userPrompt = getUserPrompt(book.getName(), outputConverter.getFormat());
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(sceneSystemPrompt);

        // Prompt 생성 시 topP 설정 추가
        Prompt prompt = new Prompt(List.of(userPrompt.createMessage(), systemPrompt.createMessage()),
                OpenAiChatOptions.builder()
                        .withMaxTokens(4095)
                        .build());

        System.out.println(prompt);

        Generation result = openAiChatModel.call(prompt).getResult();

        System.out.println(result);

        return outputConverter.convert(result.getOutput().getContent());
    }


    public List<Resource> generateImages(SceneListResponse sceneListResponse) {
        List<String> prompts = sceneListResponse.sceneResponseList().stream()
                .map(SceneResponse::description)
                .toList();

        List<Resource> imageResults = new ArrayList<>();
        for (String prompt : prompts) {
            Resource resource = imageGenerate(prompt);
            imageResults.add(resource);
        }

        return imageResults;
    }

    public List<Speech> generateAudios(SceneListResponse sceneListResponse) {
        List<String> prompts = sceneListResponse.sceneResponseList().stream()
                .map(SceneResponse::content)
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

    private PromptTemplate getUserPrompt(String title, String format) {
        return new PromptTemplate(sceneUserPrompt, Map.of("title", title, "format", format));
    }

    public Resource imageGenerate(String prompt) {
        return stabilityAiClient.generateImage(prompt, "comic-book");
    }
}
