package com.yun.partnerapi.partner;

import com.yun.partnerapi.model.Currency;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

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

    @Getter
    @Builder
    public static class Banks {
        private List<Bank> banks;
    }

    @Getter
    @Builder
    public static class Bank {
        private String name;
        private String code;
        private String branch;

    }
}
