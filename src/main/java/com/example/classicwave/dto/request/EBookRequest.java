package com.example.classicwave.dto.request;

import com.example.classicwave.domain.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
                .isbnId(this.getIsbnId())
                .likes(0L)
                .folderName(UUID.randomUUID().toString())
                .build();
    }
}