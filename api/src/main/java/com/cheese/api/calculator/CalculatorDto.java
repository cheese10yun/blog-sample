package com.cheese.api.calculator;

import lombok.Builder;
import lombok.Getter;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CalculatorDto {

    @Getter
    public static class Transaction {
        private Remittance remittance;
        private Deposit deposit;

        @Builder
        public Transaction(Remittance remittance, Deposit deposit) {
            this.remittance = remittance;
            this.deposit = deposit;
        }
    }

    @Getter
    public static class Res {
        private Remittance remittance;
        private Deposit deposit;
        private ExchangeRate exchangeRate;

        @Builder
        public Res(Remittance remittance, Deposit deposit, ExchangeRate exchangeRate) {
            this.remittance = remittance;
            this.deposit = deposit;
            this.exchangeRate = exchangeRate;
        }
    }

    @Getter
    public static class ExchangeRate {
        private double value;

        @Builder
        public ExchangeRate(double value) {
            this.value = value;
        }
    }

    @Getter
    public static class Remittance {
        private Money money;

        @Builder
        public Remittance(Money money) {
            this.money = money;
        }
    }

    @Getter
    public static class Deposit {
        private Money money;

        @Builder
        public Deposit(Money money) {
            this.money = money;
        }
    }


    @Getter
    public static class Money {
        private double amount;
        private Currency currency;
        private Locale locale;
        private String amountCurrencyFormat;

        @Builder
        public Money(double amount, Currency currency, Locale locale) {
            this.amount = amount;
            this.currency = currency;
            this.locale = locale;
            this.amountCurrencyFormat = formattingCurrency();
        }

        public void updateAmount(double amount) {
            this.amount = amount;
            this.amountCurrencyFormat = formattingCurrency();
        }

        private String formattingCurrency() {
            return NumberFormat.getCurrencyInstance(locale).format(this.amount);
        }
    }
}
