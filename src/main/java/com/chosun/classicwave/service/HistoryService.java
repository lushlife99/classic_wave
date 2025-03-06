package com.chosun.classicwave.service;

import com.chosun.classicwave.domain.Book;
import com.chosun.classicwave.domain.Quiz;
import com.chosun.classicwave.domain.QuizList;
import com.chosun.classicwave.domain.QuizSubmit;
import com.chosun.classicwave.dto.response.BookHistoryResponse;
import com.chosun.classicwave.dto.response.QuizHistoryResponse;
import com.chosun.classicwave.dto.response.QuizSubmitHistoryResponse;
import com.chosun.classicwave.repository.QuizSubmitRepository;
import com.example.classicwave.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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
                        book.getId(),
                        book.getName(),
                        book.getAuthorName(),
                        book.getPublishedYear(),
                        submit.getId()  // submitListId 설정
                ));
            }
        }
        return bookList;
    }

    @Transactional(readOnly = true)
    public QuizSubmitHistoryResponse getQuizSubmitHistory(Long summitId) {

        // 제출된 퀴즈 제출 정보를 찾습니다.
        QuizSubmit quizSubmit = quizSubmitRepository.findById(summitId)
                .orElseThrow(() -> new RuntimeException("해당 제출 내역을 찾을 수 없습니다."));

        // 제출된 퀴즈 리스트를 가져옵니다.
        QuizList quizList = quizSubmit.getQuizList();
        if (quizList == null) {
            throw new RuntimeException("해당 제출 내역에 대한 퀴즈 리스트를 찾을 수 없습니다.");
        }

        // 퀴즈 히스토리 응답 리스트를 생성합니다.
        List<QuizHistoryResponse> quizHistoryList = new ArrayList<>();

        // 제출된 답변 리스트를 가져옵니다.
        List<Integer> submitAnswerList = quizSubmit.getSubmitAnswerList();

        // 퀴즈 리스트 내 각 퀴즈에 대해 히스토리 응답을 생성합니다.
        for (int i = 0; i < quizList.getQuizzes().size(); i++) {
            Quiz quiz = quizList.getQuizzes().get(i);

            // 사용자가 제출한 해당 퀴즈의 답변을 가져옵니다.
            int userAnswer = submitAnswerList.size() > i ? submitAnswerList.get(i) : -1;

            // 퀴즈 히스토리 응답을 생성합니다.
            QuizHistoryResponse quizHistoryResponse = new QuizHistoryResponse(
                    quiz.getQuestion(),   // 퀴즈 질문
                    Collections.singletonList(userAnswer), // 사용자가 제출한 답변 (리스트에 하나의 요소로 넣음)
                    quiz.getAnswer(),     // 정답
                    quiz.getComment()     // 해설
            );

            // 히스토리 응답 리스트에 추가합니다.
            quizHistoryList.add(quizHistoryResponse);
        }

        // 최종 응답 객체 생성 (총 점수 포함)
        return new QuizSubmitHistoryResponse(quizHistoryList, quizSubmit.getScore());
    }




}
