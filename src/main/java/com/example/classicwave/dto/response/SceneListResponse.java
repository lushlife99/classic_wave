package com.example.classicwave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SceneListResponse(
        @JsonProperty(required = true, value = "total of 10 scene-list") List<SceneResponse> sceneResponseList,
        @JsonProperty(required = true, value = "book-title") String bookTitle
        ) {


}
