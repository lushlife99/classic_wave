package com.example.classicwave.openFeign.stabilityai.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequest {
    private String String;
    private int width;
    private int height;
}
