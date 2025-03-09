package com.chosun.classicwave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QuizListResponse(
        @JsonProperty("quiz") List<QuestionResponse> questions
) {
}