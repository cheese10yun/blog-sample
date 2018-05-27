package com.cheese.api.calculator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@AllArgsConstructor
public class ExchangeRateFactory {

    private final BankOfAmericaExchangeRate bankOfAmericaExchangeRate;
    private final ShinhanExchangeRate shinhanExchangeRate;

    public ExchangeRate getInstanceByLocale(Locale locale) {

        if (locale.equals(Locale.KOREA)) return shinhanExchangeRate;
        if (locale.equals(Locale.US)) return bankOfAmericaExchangeRate;

        throw new IllegalArgumentException("....");
    }
}
