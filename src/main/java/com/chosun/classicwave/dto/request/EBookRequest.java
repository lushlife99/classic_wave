package com.chosun.classicwave.dto.request;

import com.chosun.classicwave.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EBookRequest {

    private String isbnId;
    private String name;

    public Book toEntity() {
        return Book.builder()
                .name(this.getName())
                .folderName(UUID.randomUUID().toString())
                .sceneList(new ArrayList<>())
                .build();
    }

}