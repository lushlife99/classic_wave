package com.example.classicwave.service;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Quiz;
import com.example.classicwave.domain.QuizList;
import com.example.classicwave.domain.QuizSubmit;
import com.example.classicwave.dto.response.BookHistoryResponse;
import com.example.classicwave.dto.response.QuizHistoryResponse;
import com.example.classicwave.dto.response.QuizSubmitResponse;
import com.example.classicwave.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final QuizSubmitRepository quizSubmitRepository;

    @Transactional(readOnly = true)
    // 유저의 submit을 바탕으로 퀴즈 리스트 -> 책 찾아 반환
    public List<BookHistoryResponse> getUserBookHistory(Long userId){
        List<BookHistoryResponse> bookList = new ArrayList<>();

        List<QuizSubmit> quizSubmitList = quizSubmitRepository.findByMemberId(userId);

        for (QuizSubmit submit : quizSubmitList) {
            QuizList quizList = submit.getQuizList();
            if (quizList != null) {
                Book book = quizList.getBook();
                bookList.add(new BookHistoryResponse(
                        book.getName(),
                        book.getPublishedYear(),
                        book.getAuthorName(),
                        submit.getId()  // submitListId 설정
                ));
            }
        }
        return bookList;
    }

    @Transactional(readOnly = true)
    public List<QuizHistoryResponse> getQuizSubmitHistory(Long summitId) {

        QuizSubmit quizSubmit = quizSubmitRepository.findById(summitId)
                .orElseThrow(() -> new RuntimeException("해당 제출 내역을 찾을 수 없습니다."));

        List<QuizHistoryResponse> quizHistoryList = new ArrayList<>();

        QuizList quizList = quizSubmit.getQuizList();
        if (quizList != null) {

            for (Quiz quiz : quizList.getQuizzes()) {
                quizHistoryList.add(new QuizHistoryResponse(
                        quiz.getQuestion(),
                        quizSubmit.getSubmitAnswerList(),
                        quiz.getAnswer(),
                        quizSubmit.getScore(),
                        quiz.getComment()
                ));
            }
        }

        return quizHistoryList;
    }



}
