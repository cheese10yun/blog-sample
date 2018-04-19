package com.cheese.api.api;

import org.springframework.stereotype.Component;

@Component
public class BankOfAmericaApi {

    public ApiCommonDto.ExchangeRate consumeExchangeRate() {

        //실제 은행사 API 호출해서 환율 정보를 가져오는 코드가 작성 됩니다.
        return ApiCommonDto.ExchangeRate.builder()
                .rate(1059.999963)
                .corridor("US_TO_KOR")
                .corridor("BANK_OF_AMERICA")
                .build();
    }
}
