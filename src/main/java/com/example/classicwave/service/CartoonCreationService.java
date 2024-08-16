package com.example.classicwave.service;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import com.example.classicwave.dto.response.SceneListResponse;
import com.example.classicwave.repository.BookRepository;
import com.example.classicwave.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

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
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.StyleEnum;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CartoonCreationService {

    @Value("classpath:/prompts/scene-generation-system-message.st")
    private Resource sceneSystemPrompt;
    @Value("classpath:/prompts/scene-generation-user-message.st")
    private Resource sceneUserPrompt;

    private final BookRepository bookRepository;
    private final SceneRepository sceneRepository;
    private final OpenAiAudioSpeechModel audioSpeechModel;
    private final StabilityAiImageModel imageModel;
    private final OpenAiChatModel openAiChatModel;
    private final RedisTemplate<String, Object> redisTemplate;

    public SceneListResponse getSceneListByBookInfo(Book book) {

        List<Scene> sceneList = new ArrayList<>();
        BeanOutputConverter<SceneListResponse> outputConverter = new BeanOutputConverter<>(SceneListResponse.class);
        PromptTemplate userPrompt = getUserPrompt(book.getName(), book.getIsbnId(), outputConverter.getFormat());
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(sceneSystemPrompt);
        Prompt prompt = new Prompt(List.of(userPrompt.createMessage(), systemPrompt.createMessage()),
                OpenAiChatOptions.builder()
                        .withMaxTokens(4096)
                        .build());
        Generation result = openAiChatModel.call(prompt).getResult();
        SceneListResponse sceneListResponse = outputConverter.convert(result.getOutput().getContent());
        return sceneListResponse;
    }

    public List<ImageGeneration> generateImages(SceneListResponse sceneListResponse) {
        List<String> prompts = sceneListResponse.sceneResponseList().stream()
                .map(SceneListResponse.SceneResponse::description)
                .collect(Collectors.toList());

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
                .map(SceneListResponse.SceneResponse::plotSummary)
                .collect(Collectors.toList());

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
        PromptTemplate promptTemplate = new PromptTemplate(sceneUserPrompt, Map.of("title", title, "isbnId", isbnId, "format", format));
        return promptTemplate;
    }
}
