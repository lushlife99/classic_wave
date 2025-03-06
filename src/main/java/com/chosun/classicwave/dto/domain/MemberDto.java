package com.chosun.classicwave.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
public class MemberDto {

    private String name;
    private String introduction;
    private String profileImageUrl;
}
