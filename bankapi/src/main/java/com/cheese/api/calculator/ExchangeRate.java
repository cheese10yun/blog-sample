package com.cheese.api.calculator;

import com.cheese.api.api.ApiCommonDto;

public interface ExchangeRate {

    ApiCommonDto.ExchangeRate getExchangeRate(CalculatorDto.Transaction transaction);

}
