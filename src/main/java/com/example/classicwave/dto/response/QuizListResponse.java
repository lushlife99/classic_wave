package com.example.classicwave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QuizListResponse(
        @JsonProperty("quiz") List<QuestionResponse> questions
) {
    public record QuestionResponse(
            @JsonProperty("question") String question,
            @JsonProperty("options") Options options,
            @JsonProperty("answer") String answer,
            @JsonProperty("comment") String comment
    ) {
        public record Options(
                @JsonProperty("1") String optionA,
                @JsonProperty("2") String optionB,
                @JsonProperty("3") String optionC,
                @JsonProperty("4") String optionD,
                @JsonProperty("5") String optionE
        ) {
        }
    }
}