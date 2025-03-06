package com.chosun.classicwave.dto.response;

import java.util.List;

public record QuizListWithIdResponse(
        Long quizListId,
        List<QuizListResponse.QuestionResponse> questions
) {
}

