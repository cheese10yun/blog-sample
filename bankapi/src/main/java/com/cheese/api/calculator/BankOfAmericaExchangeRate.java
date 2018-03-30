package com.cheese.api.calculator;

import com.cheese.api.api.ApiCommonDto;
import com.cheese.api.api.BankOfAmericaApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BankOfAmericaExchangeRate implements ExchangeRate {

    private final BankOfAmericaApi bankOfAmericaApi;

    @Autowired
    public BankOfAmericaExchangeRate(BankOfAmericaApi bankOfAmericaApi) {
        this.bankOfAmericaApi = bankOfAmericaApi;
    }


    @Override
    public ApiCommonDto.ExchangeRate getExchangeRate(CalculatorDto.Transaction transaction) {
        return bankOfAmericaApi.consumeExchangeRate();
    }
}
