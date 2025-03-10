package com.chosun.classicwave.service;

import com.chosun.classicwave.entity.Book;
import com.chosun.classicwave.entity.Scene;
import com.chosun.classicwave.dto.domain.SceneDto;
import com.chosun.classicwave.dto.response.PlotListResponse;
import com.chosun.classicwave.dto.response.SceneDescriptionResponse;
import com.chosun.classicwave.error.CustomException;
import com.chosun.classicwave.error.ErrorCode;
import com.chosun.classicwave.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
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
    private static final String IMAGE_PREFIX = "/image";

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
        return scenes;

    }

    @Transactional(readOnly = true)
    public SceneDto getScene(Long sceneId) {
        Scene scene = sceneRepository.findById(sceneId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Book book = scene.getBook();
        String imageUrl = s3Service.getImageUrl(book.getFolderName() + IMAGE_PREFIX, scene.getPhotoId());

        return SceneDto.builder()
                .id(scene.getId())
                .imageUrl(imageUrl)
                .plotSummary(scene.getPlotSummary())
                .build();
        }
    }
