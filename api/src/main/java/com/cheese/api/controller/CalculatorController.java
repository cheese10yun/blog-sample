package com.cheese.api.controller;

import com.cheese.api.calculator.Calculator;
import com.cheese.api.calculator.CalculatorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.Locale;

@RestController
@RequestMapping("calculator")
public class CalculatorController {

    @Autowired
    private Calculator calculator;


    @RequestMapping(method = RequestMethod.GET)
    public CalculatorDto.Res calculate(
            @RequestParam("remittanceAmount") double remittanceAmount,
            @RequestParam("depositAmount") double depositAmount
    ) {


        return calculator.calculate(
                CalculatorDto.Transaction.builder()
                        .remittance(buildRemittance(remittanceAmount, Locale.US, Currency.getInstance(Locale.US)))
                        .deposit(buildDeposit(depositAmount, Locale.KOREA, Currency.getInstance(Locale.KOREA)))
                        .build()
        );
    }

    private CalculatorDto.Remittance buildRemittance(double amount, Locale locale, Currency currency) {
        return CalculatorDto.Remittance.builder()
                .money(buildMoney(amount, locale, currency))
                .build();
    }

    private CalculatorDto.Deposit buildDeposit(double amount, Locale locale, Currency currency) {
        return CalculatorDto.Deposit.builder()
                .money(buildMoney(amount, locale, currency))
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
