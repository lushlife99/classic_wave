package com.chosun.classicwave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PlotListResponse(
        @JsonProperty(required = true, value = "copyright this book") boolean copyRight,
        @JsonProperty(required = true, value = "plot array of length 10") List<String> plotList,
        @JsonProperty(required = true, value = "book-title") String bookTitle,
        @JsonProperty(required = true, value = "book-author") String author,
        @JsonProperty(required = true, value = "publish-year") int pubYear
        ) {

}