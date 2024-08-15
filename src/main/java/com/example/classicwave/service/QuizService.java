package com.example.classicwave.service;


import com.example.classicwave.domain.Quiz;
import com.example.classicwave.dto.response.QuizListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    @Value("classpath:/prompts/quiz-generation-system-message.st")
    private Resource quizSystemPrompt;
    @Value("classpath:/prompts/book-info-user-message.st")
    private Resource bookinfoUserPrompt;

    private final OpenAiChatModel openAiChatModel;

    public QuizListResponse createQuiz(String title, String isbnId){
        BeanOutputConverter<QuizListResponse> outputConverter = new BeanOutputConverter<>(QuizListResponse.class);
        PromptTemplate userPrompt = getUserPrompt(title, isbnId, outputConverter.getFormat());
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(quizSystemPrompt);
        Prompt prompt = new Prompt(List.of(userPrompt.createMessage(), systemPrompt.createMessage()),
                OpenAiChatOptions.builder()
                        .withMaxTokens(4096)
                        .build());
        log.info(String.valueOf(prompt));
        Generation result = openAiChatModel.call(prompt).getResult();
        QuizListResponse quizListResponse = outputConverter.convert(result.getOutput().getContent());
        return quizListResponse;
    }

    private PromptTemplate getUserPrompt(String title, String isbnId, String format){
        PromptTemplate promptTemplate = new PromptTemplate(bookinfoUserPrompt, Map.of("title", title, "isbnId", isbnId, "format", format));
        return promptTemplate;
    }


























//    public String createTestQuiz(String bookTitle, String isbnId) {
//        validateInput(bookTitle, isbnId);
//        String completePrompt = buildCompletePrompt(bookTitle, isbnId);
//
//        //요청 확인용
//        log.info(completePrompt);
//        return openAiChatModel.call(completePrompt);
//    }
//
//    private void validateInput(String bookTitle, String isbnId) {
//        if (bookTitle == null || bookTitle.trim().isEmpty() || isbnId == null || isbnId.trim().isEmpty()) {
//            throw new IllegalArgumentException("유효하지 않은 책 제목과 ISBN 입니다");
//        }
//    }
//
//    private String buildCompletePrompt(String bookTitle, String isbnId) {
//        String prompt = readQuizPromptFile();
//        return String.format(prompt, bookTitle, isbnId);
//    }
//
//    private String readQuizPromptFile() {
//        Path path = Paths.get(PROMPT_FILE_PATH);
//        try {
//            return Files.readString(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("프롬프트 파일을 읽는 중 오류 발생: " + e.getMessage());
//        }
//    }
}
