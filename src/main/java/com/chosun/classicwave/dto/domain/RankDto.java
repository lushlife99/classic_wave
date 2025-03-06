package com.chosun.classicwave.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RankDto {
    private String loginId;
    private String name;
    private int rating;
    //private String profilePictureUrl; // 사용자 사진 URL

}
