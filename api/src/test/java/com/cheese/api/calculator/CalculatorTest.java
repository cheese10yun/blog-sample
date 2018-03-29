package com.cheese.api.calculator;

import com.cheese.api.api.ApiCommonDto;
import com.cheese.api.api.BankOfAmericaApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Currency;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class CalculatorTest {

    @Mock
    private ExchangeRateFactory exchangeRateFactory;

    @Mock
    private BankOfAmericaExchangeRate bankOfAmericaExchangeRate;

    private Calculator calculator;

    private final double REMITTANCE_AMOUNT = 500;

    @Before
    public void setUp() throws Exception {
        calculator = new Calculator(exchangeRateFactory);
    }

    @Test
    public void name() {
        //given
        final CalculatorDto.Transaction transaction = CalculatorDto.Transaction.builder()
                .remittance(getSendMoney())
                .deposit(getReceiveMoney())
                .build();

        final ApiCommonDto.ExchangeRate exchangeRate = buildExchangeRate();

        given(exchangeRateFactory.getInstanceByLocale(any())).willReturn(bankOfAmericaExchangeRate);
        given(bankOfAmericaExchangeRate.getExchangeRate(transaction)).willReturn(exchangeRate);


        //when
        final CalculatorDto.Res res = calculator.calculate(transaction);

        //then
    }

    private ApiCommonDto.ExchangeRate buildExchangeRate() {
        return ApiCommonDto.ExchangeRate.builder()
                .rate(1059.999963)
                .fee(0.020)
                .corridor("US_TO_KOR")
                .company("BANK_OF_AMERICA")
                .build();
    }

    private CalculatorDto.Deposit getReceiveMoney() {
        return CalculatorDto.Deposit.builder()
                .money(buildMoney(0, Locale.KOREA, Currency.getInstance(Locale.KOREA)))
                .build();
    }

    private CalculatorDto.Remittance getSendMoney() {
        return CalculatorDto.Remittance.builder()
                .money(buildMoney(REMITTANCE_AMOUNT, Locale.US, Currency.getInstance(Locale.US)))
                .build();
    }

    private CalculatorDto.Money buildMoney(double amount, Locale locale, Currency currency) {
        return CalculatorDto.Money.builder()
                .amount(amount)
                .locale(locale)
                .currency(currency)
                .build();
    }
}