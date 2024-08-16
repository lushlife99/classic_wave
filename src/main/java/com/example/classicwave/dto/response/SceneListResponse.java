package com.example.classicwave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SceneListResponse(
        @JsonProperty(required = true, value = "scene-list") List<SceneResponse> sceneResponseList
        ) {
    public record SceneResponse(
            @JsonProperty(required = true, value = "scene-description") String description,
            @JsonProperty(required = true, value = "plot-summary") String plotSummary
    ) {

    }

}
