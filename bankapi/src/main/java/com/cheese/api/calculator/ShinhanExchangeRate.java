package com.cheese.api.calculator;

import com.cheese.api.api.ApiCommonDto;
import com.cheese.api.api.ShinhanApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShinhanExchangeRate implements ExchangeRate {

    private final ShinhanApi shinhanApi;

    @Autowired
    public ShinhanExchangeRate(ShinhanApi shinhanApi) {
        this.shinhanApi = shinhanApi;
    }


    @Override
    public ApiCommonDto.ExchangeRate getExchangeRate(CalculatorDto.Transaction transaction) {
        return shinhanApi.consumeExchangeRate();
    }
}
