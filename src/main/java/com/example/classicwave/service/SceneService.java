package com.example.classicwave.service;

import com.example.classicwave.domain.Book;
import com.example.classicwave.domain.Scene;
import com.example.classicwave.dto.response.SceneListResponse;
import com.example.classicwave.dto.response.SceneResponse;
import com.example.classicwave.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SceneService {

    private final SceneRepository sceneRepository;

    public List<Scene> saveSceneList(Book book, SceneListResponse sceneListResponse) {
        List<SceneResponse> sceneResponses = sceneListResponse.sceneResponseList();
        List<Scene> sceneList = new ArrayList<>();
        for (int i = 0; i < sceneResponses.size(); i++) {
            SceneResponse sceneResponse = sceneResponses.get(i);

            Scene scene = sceneResponse.toEntity(book);
            sceneList.add(scene);
        }

        return sceneRepository.saveAll(sceneList);
    }
}
