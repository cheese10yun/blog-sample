package com.cheese.api.api;

import org.springframework.stereotype.Component;

@Component
public class ShinhanApi {

    public ApiCommonDto.ExchangeRate consumeExchangeRate() {
        //실제 은행사 API 호출해서 환율 정보를 가져오는 코드가 작성 됩니다.
        return ApiCommonDto.ExchangeRate.builder()
                .rate(0.000943)
                .corridor("KOR_TO_US")
                .company("Shinhan")
                .build();
    }

}
