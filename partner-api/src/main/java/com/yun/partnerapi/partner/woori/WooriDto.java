package com.yun.partnerapi.partner.woori;


import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

public class WooriDto {

    @Getter
    @Builder
    public static class ExchangeRateResponse {
        private double exchangeRate;
        private Timestamp timestamp;
    }
}
