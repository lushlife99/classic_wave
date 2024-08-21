package com.example.classicwave.dto.response;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record SceneResponse(
        @JsonProperty(required = true, value = "scene-description") String description,
        @JsonProperty(required = true, value = "plot") String plot
) {

    public Scene toEntity(Book book) {
        return Scene.builder()
                .plotSummary(this.plot)
                .book(book)
                .description(this.description)
                .photoId(UUID.randomUUID().toString())
                .build();
    }
}
