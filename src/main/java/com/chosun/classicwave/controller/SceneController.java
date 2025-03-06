package com.chosun.classicwave.controller;

import com.chosun.classicwave.dto.domain.SceneDto;
import com.chosun.classicwave.service.SceneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "전자책 장면", description = "전자책을 구성하는 파일들을 제공하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scene")
public class SceneController {

    private final SceneService sceneService;

    @GetMapping("/{sceneId}")
    @Operation(summary = "전자책을 이루는 한 장면 데이터 조회", description = "장면의 사진, 음성파일, 줄거리 조회")
    public SceneDto getCartoonFiles(@PathVariable Long sceneId) {
        SceneDto scene = sceneService.getScene(sceneId);
        System.out.println("SceneController.getCartoonFiles");
        return scene;
    }
}