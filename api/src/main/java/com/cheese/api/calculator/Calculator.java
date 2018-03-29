package com.cheese.api.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class Calculator {

    private final ExchangeRateFactory exchangeRateFactory;

    @Autowired
    public Calculator(ExchangeRateFactory exchangeRateFactory) {
        this.exchangeRateFactory = exchangeRateFactory;
    }

    public CalculatorDto.Res calculate(CalculatorDto.Transaction transaction) {
        final ExchangeRate exchangeRate = getInstanceByLocale(transaction); //의존성 주입
        final double remittanceAmount = transaction.getRemittance().getMoney().getAmount();
        final double rate = exchangeRate.getExchangeRate(transaction).getRate(); //주입받은 의존성으로 파트너 은행사의 환율 정보 가져옴
        final double fee = exchangeRate.getExchangeRate(transaction).getFee(); //주입받은 의존성으로 파트너 은행사의 수수료 정보 가져옴

        calculateDepositAmount(transaction.getDeposit(), remittanceAmount, rate); // 환율 정보 기반으로 입금액 계산

        return CalculatorDto.Res.builder()
                .deposit(transaction.getDeposit())
                .remittance(transaction.getRemittance())
                .paymentInformation(buildPaymentInformation(rate, fee))
                .build();

    }

    private CalculatorDto.PaymentInformation buildPaymentInformation(double rate, double fee) {
        return CalculatorDto.PaymentInformation.builder()
                .rate(rate)
                .fee(fee)
                .build();
    }

    private void calculateDepositAmount(CalculatorDto.Deposit deposit, final double remittanceAmount, final double rate) {
        deposit.getMoney().updateAmount(remittanceAmount * rate);
    }

    private ExchangeRate getInstanceByLocale(CalculatorDto.Transaction transaction) {
        final Locale locale = transaction.getRemittance().getMoney().getLocale();
        return exchangeRateFactory.getInstanceByLocale(locale);
    }


}
