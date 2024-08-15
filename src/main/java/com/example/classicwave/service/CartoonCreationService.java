package com.example.classicwave.service;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import com.example.classicwave.repository.BookRepository;
import com.example.classicwave.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartoonCreationService {

    @Value("classpath:/prompts/scene-generation-system-message.st")
    private Resource sceneSystemPrompt;

    private final BookRepository bookRepository;
    private final SceneRepository sceneRepository;
    private final OpenAiChatModel openAiChatModel;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<Generation> getSceneListByBookInfo(String title, String isbnId) {
        List<Scene> sceneList = new ArrayList<>();
        Message userPrompt = getUserPrompt(title, isbnId);
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(sceneSystemPrompt);
        Prompt prompt = new Prompt(List.of(userPrompt, systemPrompt.createMessage()),
                OpenAiChatOptions.builder()
                        .withMaxTokens(4096)
                        .build());
        List<Generation> response = openAiChatModel.call(prompt).getResults();
        System.out.println(response);
        return response;
    }

    private Message getUserPrompt(String title, String isbnId) {
        String message = "title : " + title + "\n" + "isbnId : " + isbnId;
        return new UserMessage(message);
    }
}
