package com.chosun.classicwave.dto.response;

import com.chosun.classicwave.domain.Book;
import com.chosun.classicwave.domain.Scene;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record SceneResponse(
        @JsonProperty(required = true, value = "scene-description in english short one sentence") String description,
        @JsonProperty(required = true, value = "Long synopsis of at least 300 characters in korean language") String content
) {

    public Scene toEntity(Book book) {
        return Scene.builder()
                .plotSummary(this.content)
                .book(book)
                .description(this.description)
                .photoId(UUID.randomUUID().toString())
                .build();
    }
}