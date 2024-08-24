package com.example.classicwave.service;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import com.example.classicwave.dto.domain.SceneDto;
import com.example.classicwave.dto.response.PlotListResponse;
import com.example.classicwave.dto.response.SceneDescriptionResponse;
import com.example.classicwave.dto.response.SceneResponse;
import com.example.classicwave.error.CustomException;
import com.example.classicwave.error.ErrorCode;
import com.example.classicwave.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SceneService {

    private final SceneRepository sceneRepository;
    private final S3FileUploadService s3Service;

    @Transactional
    public List<Scene> saveSceneList(Book book, SceneDescriptionResponse sceneListResponse, PlotListResponse plotListResponse) {
        List<Scene> sceneList = new ArrayList<>();

        for(int i = 0; i < plotListResponse.plotList().size(); i++) {
            String plot = plotListResponse.plotList().get(i);
            String description = sceneListResponse.descriptionList().get(i);

            Scene scene = Scene.builder()
                    .plotSummary(plot)
                    .book(book)
                    .description(description)
                    .photoId(UUID.randomUUID().toString())
                    .build();

            sceneList.add(scene);
        }
        List<Scene> scenes = sceneRepository.saveAll(sceneList);
        book.setSceneList(scenes);
        return scenes;

    }

    @Transactional(readOnly = true)
    public SceneDto getScene(Long sceneId) {
        Scene scene = sceneRepository.findById(sceneId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Book book = scene.getBook();
        Resource image = s3Service.getImage(book.getFolderName(), scene.getPhotoId());
        Resource audio = s3Service.getAudio(book.getFolderName(), scene.getPhotoId());

        return SceneDto.builder()
                .id(scene.getId())
                .audioFile(audio)
                .image(image)
                .plotSummary(scene.getPlotSummary())
                .build();
    }
}
