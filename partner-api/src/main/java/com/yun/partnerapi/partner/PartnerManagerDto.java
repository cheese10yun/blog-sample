package com.yun.partnerapi.partner;

import com.yun.partnerapi.model.Currency;
import lombok.Builder;
import lombok.Getter;

public class PartnerManagerDto {

    @Getter
    @Builder
    public static class ExchangeRate {
        private double rate;
        private ExchangeCurrency exchangeCurrency;
    }

    @Getter
    @Builder
    public static class ExchangeCurrency {
        private Currency srcCurrency;
        private Currency dstCurrency;
    }
}
