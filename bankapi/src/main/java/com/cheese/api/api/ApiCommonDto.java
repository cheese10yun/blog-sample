package com.cheese.api.api;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

public class ApiCommonDto {

    @Getter
    public static class ExchangeRate {

        private double rate;
        private String corridor;
        private String company;
        private Date date;

        @Builder
        public ExchangeRate(double rate, String corridor, String company) {
            this.rate = rate;
            this.corridor = corridor;
            this.company = company;
            this.date = new Date(System.currentTimeMillis());
        }
    }
}
