package com.example.classicwave.service;

import com.example.classicwave.domain.Scene;
import com.example.classicwave.dto.response.SceneListResponse;
import com.example.classicwave.repository.BookRepository;
import com.example.classicwave.repository.SceneRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CartoonCreationService {

    @Value("classpath:/prompts/scene-generation-system-message.st")
    private Resource sceneSystemPrompt;
    @Value("classpath:/prompts/book-info-user-message.st")
    private Resource sceneUserPrompt;

    private final BookRepository bookRepository;
    private final SceneRepository sceneRepository;
    private final OpenAiChatModel openAiChatModel;
    private final RedisTemplate<String, Object> redisTemplate;

    public SceneListResponse getSceneListByBookInfo(String title, String isbnId) {
        List<Scene> sceneList = new ArrayList<>();
        BeanOutputConverter<SceneListResponse> outputConverter = new BeanOutputConverter<>(SceneListResponse.class);
        PromptTemplate userPrompt = getUserPrompt(title, isbnId, outputConverter.getFormat());
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(sceneSystemPrompt);
        Prompt prompt = new Prompt(List.of(userPrompt.createMessage(), systemPrompt.createMessage()),
                OpenAiChatOptions.builder()
                        .withMaxTokens(4096)
                        .build());
        Generation result = openAiChatModel.call(prompt).getResult();
        SceneListResponse sceneListResponse = outputConverter.convert(result.getOutput().getContent());
        return sceneListResponse;
    }

    private PromptTemplate getUserPrompt(String title, String isbnId, String format) {
        PromptTemplate promptTemplate = new PromptTemplate(sceneUserPrompt, Map.of("title", title, "isbnId", isbnId, "format", format));
        return promptTemplate;
    }
}
