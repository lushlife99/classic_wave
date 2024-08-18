package com.example.classicwave.service;

import com.example.classicwave.domain.Member;
import com.example.classicwave.domain.Quiz;
import com.example.classicwave.domain.QuizList;
import com.example.classicwave.domain.QuizSubmit;
import com.example.classicwave.dto.response.QuizListResponse;
import com.example.classicwave.dto.response.QuizSubmitResponse;
import com.example.classicwave.error.CustomException;
import com.example.classicwave.error.ErrorCode;
import com.example.classicwave.jwt.JwtTokenProvider;
import com.example.classicwave.repository.*;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {


    @Value("classpath:/prompts/quiz-generation-system-message.st")
    private Resource quizSystemPrompt;
    @Value("classpath:/prompts/book-info-user-message.st")
    private Resource bookinfoUserPrompt;

    private final JwtTokenProvider jwtTokenProvider;
    private final OpenAiChatModel openAiChatModel;
    private final QuizRepository quizRepository;
    private final QuizListRepository quizListRepository;
    private final QuizSubmitRepository quizSubmitRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;



    //퀴즈 생성
    @Transactional
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

    //퀴즈 생성과 저장
    @Transactional
    public QuizListResponse createAndSaveQuiz(String title, String isbnId) {

        QuizListResponse quizListResponse = createQuiz(title, isbnId);

        // Book 엔티티 조회 <- 구현 시 주석 풀 것
        // Book book = bookRepository.findByIsbnId(isbnId)
        //        .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbnId));

        QuizList quizList = new QuizList();
        //quizList.setBook(book);


        quizListRepository.save(quizList);

        for (QuizListResponse.QuestionResponse questionResponse : quizListResponse.questions()) {
            Quiz quiz = new Quiz();
            quiz.setQuestion(questionResponse.question());
            quiz.setAnswer(parseAnswer(questionResponse.answer()));
            quiz.setQuizList(quizList);

            List<String> optionList = new ArrayList<>();
            optionList.add(questionResponse.options().optionA());
            optionList.add(questionResponse.options().optionB());
            optionList.add(questionResponse.options().optionC());
            optionList.add(questionResponse.options().optionD());
            optionList.add(questionResponse.options().optionE());

            quiz.setOptionList(optionList);

            quizRepository.save(quiz);
        }

        return quizListResponse;
    }

    private int parseAnswer(String answer) {
        return Integer.parseInt(answer.split(":")[0].trim());
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

        // Member 객체를 가져오기 위해 memberRepository에서 다시 조회
        Member currentMember = memberRepository.findByLogInId(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        QuizSubmit quizSubmit = QuizSubmit.builder()
                .member(currentMember)
                .quizList(quizList)
                .submitAnswerList(userAnswers)
                .score(score)
                .build();

        quizSubmitRepository.save(quizSubmit);

        return new QuizSubmitResponse(score);

    }

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
}

