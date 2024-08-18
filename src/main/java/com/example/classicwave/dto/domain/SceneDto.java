package com.example.classicwave.dto.domain;

import com.example.classicwave.domain.Scene;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SceneDto {

    private Long id;
    private Resource image;
    private String plotSummary;
    private Resource audioFile;

    public SceneDto(Scene scene) {
        this.id = scene.getId();
        this.plotSummary = scene.getPlotSummary();
    }
}
