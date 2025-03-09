package com.chosun.classicwave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QuestionResponse(
        @JsonProperty("question") String question,
        @JsonProperty("options") List<String> options,
        @JsonProperty("answer") String answer,
        @JsonProperty("comment") String comment
) {
}
