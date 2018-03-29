package com.cheese.api.api;

import org.springframework.stereotype.Component;

@Component
public class BankOfAmericaApi {

    public ApiCommonDto.ExchangeRate consumeExchangeRate() {
        return ApiCommonDto.ExchangeRate.builder()
                .rate(1059.999963)
                .corridor("US_TO_KOR")
                .corridor("BANK_OF_AMERICA")
                .build();
    }
}
