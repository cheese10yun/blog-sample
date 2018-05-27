package com.cheese.api.calculator;

import com.cheese.api.api.ApiCommonDto;
import com.cheese.api.api.BankOfAmericaApi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class BankOfAmericaExchangeRate implements ExchangeRate {

    private BankOfAmericaApi bankOfAmericaApi;

    @Override
    public ApiCommonDto.ExchangeRate getExchangeRate(CalculatorDto.Transaction transaction) {
        return bankOfAmericaApi.consumeExchangeRate();
    }
}
