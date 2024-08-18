package com.example.classicwave.controller;

import com.example.classicwave.dto.domain.SceneDto;
import com.example.classicwave.service.S3FileUploadService;
import com.example.classicwave.service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scene")
public class SceneController {

    private final SceneService sceneService;

    @GetMapping("/{sceneId}")
    public SceneDto getCartoonFiles(@PathVariable Long sceneId) {
        return sceneService.getScene(sceneId);
    }
}