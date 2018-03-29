package com.cheese.api.api;

import org.springframework.stereotype.Component;

@Component
public class ShinhanApi {

    public ApiCommonDto.ExchangeRate consumeExchangeRate() {
        return ApiCommonDto.ExchangeRate.builder()
                .rate(0.000943)
                .corridor("KOR_TO_US")
                .company("Shinhan")
                .build();
    }

}
