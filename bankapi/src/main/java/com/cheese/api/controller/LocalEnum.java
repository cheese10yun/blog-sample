package com.cheese.api.controller;

import lombok.Getter;

import java.util.Locale;

@Getter
public enum LocalEnum {

    US(Locale.US),
    KR(Locale.KOREA);

    private Locale locale;

    LocalEnum(Locale locale) {
        this.locale = locale;
    }
}
