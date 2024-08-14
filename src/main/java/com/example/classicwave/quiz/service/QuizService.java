package com.example.classicwave.quiz.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final OpenAiChatModel openAiChatModel;
    private static final String PROMPT_FILE_PATH = "prompt/Quizprompt.txt";

    public String createTestQuiz(String bookTitle, String isbnId) {
        validateInput(bookTitle, isbnId);
        String completePrompt = buildCompletePrompt(bookTitle, isbnId);

        //요청 확인용
        log.info(completePrompt);
        return openAiChatModel.call(completePrompt);
    }

    private void validateInput(String bookTitle, String isbnId) {
        if (bookTitle == null || bookTitle.trim().isEmpty() || isbnId == null || isbnId.trim().isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 책 제목과 ISBN 입니다");
        }
    }

    private String buildCompletePrompt(String bookTitle, String isbnId) {
        String prompt = readQuizPromptFile();
        return String.format(prompt, bookTitle, isbnId);
    }

    private String readQuizPromptFile() {
        Path path = Paths.get(PROMPT_FILE_PATH);
        try {
            return Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("프롬프트 파일을 읽는 중 오류 발생: " + e.getMessage());
        }
    }
}
