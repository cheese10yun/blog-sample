package com.cheese.api.controller;

import lombok.Getter;

import java.util.Currency;
import java.util.Locale;

@Getter
public enum CurrencyEnum {

    USD(Currency.getInstance(Locale.US)),
    KRW(Currency.getInstance(Locale.KOREA));

    private Currency currency;

    CurrencyEnum(Currency currency) {
        this.currency = currency;
    }
}
