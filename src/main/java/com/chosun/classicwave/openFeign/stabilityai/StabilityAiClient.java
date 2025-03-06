package com.chosun.classicwave.openFeign.stabilityai;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "stabilityAiClient",
        url = "https://api.stability.ai/v2beta/stable-image/generate",
        configuration = StabilityAiConfig.class
)
public interface StabilityAiClient {

    @PostMapping(value = "/core", consumes = "multipart/form-data")
    Resource generateImage(@RequestPart("prompt") String prompt, @RequestPart("style_preset") String stylePreset);
}