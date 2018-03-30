package com.cheese.api.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ExchangeRateFactory {

    private final BankOfAmericaExchangeRate bankOfAmericaExchangeRate;
    private final ShinhanExchangeRate shinhanExchangeRate;

    @Autowired
    public ExchangeRateFactory(BankOfAmericaExchangeRate bankOfAmericaExchangeRate, ShinhanExchangeRate shinhanExchangeRate) {
        this.bankOfAmericaExchangeRate = bankOfAmericaExchangeRate;
        this.shinhanExchangeRate = shinhanExchangeRate;
    }

    public ExchangeRate getInstanceByLocale(Locale locale) {

        if (locale.equals(Locale.KOREA)) return shinhanExchangeRate;
        if (locale.equals(Locale.US)) return bankOfAmericaExchangeRate;

        throw new IllegalArgumentException("....");
    }
}
