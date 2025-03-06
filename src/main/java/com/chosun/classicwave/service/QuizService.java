package com.chosun.classicwave.service;

import com.chosun.classicwave.domain.*;
import com.chosun.classicwave.repository.*;
import com.chosun.classicwave.dto.response.QuizListResponse;
import com.chosun.classicwave.dto.response.QuizListWithIdResponse;
import com.chosun.classicwave.dto.response.QuizSubmitResponse;
import com.chosun.classicwave.error.CustomException;
import com.chosun.classicwave.error.ErrorCode;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Todo
 * 1. 퀴즈 생성 시 오류를 명확히 반환
 * 2. 퀴즈 생성할 때 오류 발생 시 후처리 생각
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    @Value("classpath:/prompts/quiz-generation-system-message.st")
    private Resource quizSystemPrompt;
    @Value("classpath:/prompts/book-info-user-message.st")
    private Resource bookInfoUserPrompt;

    private final OpenAiChatModel openAiChatModel;
    private final QuizRepository quizRepository;
    private final QuizListRepository quizListRepository;
    private final QuizSubmitRepository quizSubmitRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Transactional
    public QuizListWithIdResponse getQuizList(String title){

        Book book = bookRepository.findByName(title).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        QuizList existingQuizList = quizListRepository.findByBook(book).orElse(null);
        if(existingQuizList != null){ // 해당 책에 퀴즈가 존재한다면 해당 퀴즈 리스트를 반환
            return getExistQuizList(existingQuizList);
        }

        return createAndSaveQuiz(title); // 해당 책에 퀴즈가 존재하지 않는다면 퀴즈를 생성
    }

    // 기존 퀴즈 목록을 반환
    @Transactional
    public QuizListWithIdResponse getExistQuizList(QuizList quizList){
        List<QuizListResponse.QuestionResponse> questions = quizList.getQuizzes().stream()
                .map(quiz -> {
                    List<String> options = quiz.getOptionList();
                    // 정답 번호를 문자열로 반환
                    String answerOption = String.valueOf(quiz.getAnswer()); // 0-based index to 1-based
                    return new QuizListResponse.QuestionResponse(
                            quiz.getQuestion(),
                            options,
                            answerOption, // 정답 번호를 문자열로 반환
                            quiz.getComment()
                    );
                })
                .collect(Collectors.toList());

        return new QuizListWithIdResponse(quizList.getId(), questions);
    }


    // 퀴즈 생성

    public QuizListResponse createQuiz(String title){

        BeanOutputConverter<QuizListResponse> outputConverter = new BeanOutputConverter<>(QuizListResponse.class);
        PromptTemplate userPrompt = getUserPrompt(title,outputConverter.getFormat());
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(quizSystemPrompt);
        Prompt prompt = new Prompt(List.of(userPrompt.createMessage(), systemPrompt.createMessage()),
                OpenAiChatOptions.builder()
                        .withMaxTokens(4096)
                        .build());
        Generation result = openAiChatModel.call(prompt).getResult();
        QuizListResponse quizListResponse = outputConverter.convert(result.getOutput().getContent());
        
        return quizListResponse;
    }

    private PromptTemplate getUserPrompt(String title,  String format){
        PromptTemplate promptTemplate = new PromptTemplate(bookInfoUserPrompt, Map.of("title", title,"format", format));
        return promptTemplate;
    }

    // 퀴즈 생성과 저장
    public QuizListWithIdResponse createAndSaveQuiz(String title) {

        Book book = bookRepository.findByName(title).orElseThrow(() -> new RuntimeException("해당 책이 존재하지 않습니다.: " + title));
        QuizListResponse quizListResponse = createQuiz(title);

        QuizList quizList = QuizList.from(book);
        quizListRepository.save(quizList);

        for (QuizListResponse.QuestionResponse questionResponse : quizListResponse.questions()) {
            Quiz quiz = Quiz.from(questionResponse, quizList, questionResponse.options(), parseAnswer(questionResponse.answer()));
            quizRepository.save(quiz);
        }

        return new QuizListWithIdResponse(quizList.getId(), quizListResponse.questions());
    }



    // 퀴즈 제출 후 채점
    public QuizSubmitResponse submitQuiz(Long quizListId, List<Integer> userAnswers){

        QuizList quizList = quizListRepository.findById(quizListId)
                .orElseThrow(()-> new CustomException(ErrorCode.BAD_REQUEST));

        List<Quiz> quizzes = quizRepository.findByQuizList(quizList);
        List<Integer> correctAnswers = new ArrayList<>();
        for(Quiz quiz : quizzes){
            correctAnswers.add(quiz.getAnswer());
        }

        int score = calculateScore(userAnswers,correctAnswers);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Member currentMember = memberRepository.findByLogInId(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        currentMember.setRating(currentMember.getRating() + score); // 점수 추가
        memberRepository.save(currentMember);


        QuizSubmit quizSubmit = QuizSubmit.builder()
                .member(currentMember)
                .quizList(quizList)
                .submitAnswerList(userAnswers)
                .score(score)
                .build();

        quizSubmitRepository.save(quizSubmit);

        for (int i = 0; i < quizzes.size(); i++) {
            Quiz quiz = quizzes.get(i);
            quiz.setSubmitCount(quiz.getSubmitCount() + 1); // 제출 횟수 증가
            if (userAnswers.get(i).equals(quiz.getAnswer())) {
                quiz.setCorrectCount(quiz.getCorrectCount() + 1); // 정답 횟수 증가
            }
            quizRepository.save(quiz); // 퀴즈 업데이트
        }

        return new QuizSubmitResponse(score);

    }
    // 점수 계산
    private int calculateScore(List<Integer> userAnswers, List<Integer> correctAnswers){
        if(userAnswers.size()!= correctAnswers.size()){
            throw new IllegalArgumentException("제출된 답안의 갯수가 일치하지 않습니다.");
        }

        int correctCount =0;

        for(int i=0;i<userAnswers.size(); i++){
            if(userAnswers.get(i).equals(correctAnswers.get(i))){
                correctCount++;
            }
        }
        return correctCount;
    }

    // 퀴즈별 정답률 계산
    private void updateQuizStatistics(Quiz quiz, int score) {
        quiz.setSubmitCount(quiz.getSubmitCount() + 1);
        if (score > 0) {
            quiz.setCorrectCount(quiz.getCorrectCount() + score);
        }
        quizRepository.save(quiz);
    }

    private int parseAnswer(String answer) {
        return Integer.parseInt(answer.split(":")[0].trim());
    }
}

