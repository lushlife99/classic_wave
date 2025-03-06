package com.chosun.classicwave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public record SceneDescriptionResponse(
        @JsonProperty(required = true, value = "Array of scene description prompts of length 10") List<String> descriptionList
) {

}
