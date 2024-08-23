package com.example.classicwave.service;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import com.example.classicwave.dto.request.EBookRequest;
import com.example.classicwave.dto.response.PlotListResponse;
import com.example.classicwave.dto.response.SceneDescriptionResponse;
import com.example.classicwave.openFeign.gutenberg.GutenbergApiClient;
import com.example.classicwave.openFeign.stabilityai.StabilityAiClient;
import com.example.classicwave.repository.BookRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

            PlotListResponse plotListResponse = getPlotListAndBookInfo(bookRequest);
            System.out.println("plotListResponse = " + plotListResponse);
            SceneDescriptionResponse sceneDescriptions = getSceneDescriptions(plotListResponse);
            System.out.println("sceneDescriptions = " + sceneDescriptions);
            Book book = bookService.saveBook(bookRequest, plotListResponse);

//            if (sceneListResponse.copyRight()) {
//                throw new CustomException(ErrorCode.COPYRIGHT_BOOK);
//            }

//            // 3. Create SceneList
            List<Scene> scenes = sceneService.saveSceneList(book, sceneDescriptions, plotListResponse);
//
            List<Resource> images = generateImages(sceneDescriptions);
            s3Service.uploadImages(images, book.getFolderName() + IMAGE_PREFIX);

//            List<Speech> speeches = generateAudios(sceneListResponse);
//            s3Service.uploadAudios(speeches, book.getFolderName() + AUDIO_PREFIX);

            book.setSceneList(scenes);
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

//    public SceneListResponse getSceneListByBookInfo(EBookRequest bookRequest) {
//
//        BeanOutputConverter<SceneListResponse> outputConverter = new BeanOutputConverter<>(SceneListResponse.class);
//        PromptTemplate userPrompt = getUserPrompt(bookRequest.getName(), outputConverter.getFormat());
//        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(sceneSystemPrompt);
//
//        // Prompt 생성 시 topP 설정 추가
//        Prompt prompt = new Prompt(List.of(userPrompt.createMessage(), systemPrompt.createMessage()),
//                OpenAiChatOptions.builder()
//                        .withMaxTokens(4095)
//                        .build());
//
//        System.out.println(prompt);
//
//        Generation result = openAiChatModel.call(prompt).getResult();
//
//        System.out.println(result);
//
//        return outputConverter.convert(result.getOutput().getContent());
//    }


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

    private PromptTemplate getUserPrompt(String title, String format) {
        return new PromptTemplate(sceneUserPrompt, Map.of("title", title, "format", format));
    }

    public Resource imageGenerate(String prompt) {

        return stabilityAiClient.generateImage(prompt, "comic-book");
    }
}
