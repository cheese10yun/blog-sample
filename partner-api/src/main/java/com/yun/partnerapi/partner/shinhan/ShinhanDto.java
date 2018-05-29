package com.yun.partnerapi.partner.shinhan;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

public class ShinhanDto {

    @Getter
    @Builder
    public static class ExchangeRateResponse {
        private double fxRate;
        private String EventId;
        private Timestamp DateTime;
    }
}
