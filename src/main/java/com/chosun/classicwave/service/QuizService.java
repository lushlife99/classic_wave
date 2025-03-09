package com.chosun.classicwave.service;

import com.chosun.classicwave.entity.*;
import com.chosun.classicwave.dto.response.QuestionResponse;
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

        return createAndSaveQuiz(title); // 해당 책에 퀴즈가 존재하지 않는다면 퀴즈를 생성한 뒤 반환
    }

    // 기존 퀴즈 목록을 반환
    @Transactional
    public QuizListWithIdResponse getExistQuizList(QuizList quizList){
        List<QuestionResponse> questions = quizList.getQuizzes().stream()
                .map(quiz -> {
                    List<String> options = quiz.getOptionList();
                    return new QuestionResponse(
                            quiz.getQuestion(),
                            options,
                            String.valueOf(quiz.getAnswer()),
                            quiz.getComment()
                    );
                })
                .collect(Collectors.toList());

        return new QuizListWithIdResponse(quizList.getId(), questions);
    }


    // 퀴즈 생성
    @Transactional
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
    @Transactional
    public QuizListWithIdResponse createAndSaveQuiz(String title) {

        Book book = bookRepository.findByName(title).orElseThrow(() -> new RuntimeException("해당 책이 존재하지 않습니다.: " + title));
        QuizListResponse quizListResponse = createQuiz(title);

        QuizList quizList = QuizList.from(book);
        quizListRepository.save(quizList);

        for (QuestionResponse questionResponse : quizListResponse.questions()) {
            Quiz quiz = new Quiz(quizList, questionResponse.question(), questionResponse.options(),
                    parseAnswer(questionResponse.answer()), questionResponse.comment());
            quizRepository.save(quiz);
        }

        return new QuizListWithIdResponse(quizList.getId(), quizListResponse.questions());
    }


    /**
     * 나중에 지우기. 메모용
     *
     * 퀴즈 제출 채점
     * 1. 퀴즈리스트 조회
     * 2. 퀴즈 조회
     * 3. 점수 채점
     * 4. 회원 총 점수 증가
     * 5. 퀴즈 제출 저장
     * 6. 퀴즈에 제출, 정답 횟수 증가
     *
     * 일단 5~6 순서를 변경해야할듯.
     * 4번에서 setter 쓰지 않기
     * 함수 쪼개기
     * 트랜잭션 분리
     *
     * @param quizListId
     * @param userAnswers
     * @param userId
     * @return
     */

    // 퀴즈 제출 후 채점
    @Transactional
    public QuizSubmitResponse submitQuiz(Long quizListId, List<Integer> userAnswers, long userId){ // 함수 너무 긺. 리팩토링

        QuizList quizList = quizListRepository.findById(quizListId).orElseThrow(()-> new CustomException(ErrorCode.BAD_REQUEST));

        List<Quiz> quizzes = quizRepository.findByQuizList(quizList);
        List<Integer> correctAnswers = new ArrayList<>();
        for(Quiz quiz : quizzes){
            correctAnswers.add(quiz.getAnswer());
        }

        int score = calculateScore(userAnswers,correctAnswers);
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        member.plusRating(score);

        QuizSubmit quizSubmit = QuizSubmit.builder()
                .member(member)
                .quizList(quizList)
                .submitAnswerList(userAnswers)
                .score(score)
                .build();

        quizSubmitRepository.save(quizSubmit);

        for (int i = 0; i < quizzes.size(); i++) {
            Quiz quiz = quizzes.get(i);
            quiz.plusSubmitCount(); // 제출 횟수 증가
            if (userAnswers.get(i).equals(quiz.getAnswer())) {
                quiz.plusCorrectCount(); //정답 횟수 증가
            }
        }

        return new QuizSubmitResponse(score);

    }
    // 점수 계산
    private int calculateScore(List<Integer> userAnswers, List<Integer> correctAnswers){
        if(userAnswers.size()!= correctAnswers.size()){
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        int correctCount =0;

        for(int i = 0; i < userAnswers.size(); i++){
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

