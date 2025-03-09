package com.chosun.classicwave.service;

import com.chosun.classicwave.entity.Book;
import com.chosun.classicwave.dto.request.EBookRequest;
import com.chosun.classicwave.dto.response.PlotListResponse;
import com.chosun.classicwave.dto.response.SceneDescriptionResponse;
import com.chosun.classicwave.openFeign.gutenberg.GutenbergApiClient;
import com.chosun.classicwave.openFeign.stabilityai.StabilityAiClient;
import com.chosun.classicwave.repository.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartoonCreationService {

    @Value("classpath:/prompts/scene-description-system-prompt.st")
    private Resource sceneDescriptionSystemPrompt;
    @Value("classpath:/prompts/plot-generation-system-message.st")
    private Resource plotSystemPrompt;
    @Value("classpath:/prompts/scene-generation-user-message.st")
    private Resource sceneDescriptionUserPrompt;

    @Value("classpath:/prompts/book-info-user-message.st")
    private Resource sceneUserPrompt;
    private final static String IMAGE_PREFIX = "/image";
    private final static String AUDIO_PREFIX = "/audio";

    private final StabilityAiClient stabilityAiClient;
    private final BookRepository bookRepository;
    private final S3FileUploadService s3Service;
    private final SceneService sceneService;
    private final OpenAiAudioSpeechModel audioSpeechModel;
    private final OpenAiChatModel openAiChatModel;
    private final RedisTemplate<String, EBookRequest> redisTemplate;

    @Transactional
    public void createCartoon(String key) throws IOException {
        EBookRequest bookRequest = redisTemplate.opsForSet().pop(key);
        Optional<Book> optionalBook = bookRepository.findByName(bookRequest.getName());

        if (optionalBook.isEmpty()) {
            PlotListResponse plotListResponse = getPlotListAndBookInfo(bookRequest);
            SceneDescriptionResponse sceneDescriptions = getSceneDescriptions(plotListResponse);

            Book book = saveBookAndScenes(bookRequest, plotListResponse, sceneDescriptions);
            List<Resource> images = generateImages(sceneDescriptions);

            s3Service.uploadImages(images, book.getFolderName() + IMAGE_PREFIX);
        }
    }

    public PlotListResponse getPlotListAndBookInfo(EBookRequest eBookRequest) {

        BeanOutputConverter<PlotListResponse> outputConverter = new BeanOutputConverter<>(PlotListResponse.class);
        PromptTemplate userPrompt = getUserPrompt(eBookRequest.getName(), outputConverter.getFormat());
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(plotSystemPrompt);
        Prompt prompt = new Prompt(List.of(userPrompt.createMessage(), systemPrompt.createMessage()),
                OpenAiChatOptions.builder()
                        .withMaxTokens(4095)
                        .build());

        Generation result = openAiChatModel.call(prompt).getResult();
        return outputConverter.convert(result.getOutput().getContent());
    }

    public SceneDescriptionResponse getSceneDescriptions(PlotListResponse plotListResponse) {

        BeanOutputConverter<SceneDescriptionResponse> outputConverter = new BeanOutputConverter<>(SceneDescriptionResponse.class);
        PromptTemplate userPrompt = generateSceneUserPrompt(plotListResponse, outputConverter.getFormat());
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(sceneDescriptionSystemPrompt);
        Prompt prompt = new Prompt(List.of(userPrompt.createMessage(), systemPrompt.createMessage()),
                OpenAiChatOptions.builder()
                        .withMaxTokens(4095)
                        .build());


        Generation result = openAiChatModel.call(prompt).getResult();
        return outputConverter.convert(result.getOutput().getContent());
    }

    public List<Resource> generateImages(SceneDescriptionResponse sceneDescriptionResponse) {
        List<String> prompts = sceneDescriptionResponse.descriptionList();

        List<Resource> imageResults = new ArrayList<>();
        for (String prompt : prompts) {
            Resource resource = imageGenerate(prompt);
            imageResults.add(resource);
        }

        return imageResults;
    }

    public List<Speech> generateAudios(PlotListResponse plotListResponse) {
        List<String> prompts = plotListResponse.plotList();

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

    public PromptTemplate generateSceneUserPrompt(PlotListResponse response, String format) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String plot : response.plotList()) {
            stringBuilder.append("plot: " + plot + "\n");
        }

        return new PromptTemplate(sceneDescriptionUserPrompt, Map.of("summary", stringBuilder.toString(), "format", format));
    }

    public Resource imageGenerate(String prompt) {
        return stabilityAiClient.generateImage(prompt, "comic-book");
    }

    private PromptTemplate getUserPrompt(String title, String format) {
        return new PromptTemplate(sceneUserPrompt, Map.of("title", title, "format", format));
    }

    private Book saveBookAndScenes(EBookRequest bookRequest, PlotListResponse plotListResponse, SceneDescriptionResponse sceneDescriptions) {
        Book book = Book.builder()
                .name(bookRequest.getName())
                .authorName(plotListResponse.author())
                .publishedYear(plotListResponse.pubYear())
                .build();

        bookRepository.save(book);
        sceneService.saveSceneList(book, sceneDescriptions, plotListResponse);
        return book;
    }

}