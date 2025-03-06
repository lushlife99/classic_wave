package com.chosun.classicwave.dto.domain;

import com.chosun.classicwave.domain.Scene;
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
    private String imageUrl;
    private String plotSummary;
    private Resource audioFile;

    public SceneDto(Scene scene) {
        this.id = scene.getId();
        this.plotSummary = scene.getPlotSummary();
    }
}
