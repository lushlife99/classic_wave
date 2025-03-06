package com.chosun.classicwave.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Constant {

    REFRESH_TOKEN_COOKIE_VALUE("refreshToken");

    String value;
}
