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
        private PaymentInformation paymentInformation;

        @Builder
        public Res(Remittance remittance, Deposit deposit, PaymentInformation paymentInformation) {
            this.remittance = remittance;
            this.deposit = deposit;
            this.paymentInformation = paymentInformation;
        }
    }

    @Getter
    public static class PaymentInformation {
        private double fee;
        private double rate;

        @Builder
        public PaymentInformation(double fee, double rate) {
            this.fee = fee;
            this.rate = rate;
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
        private String currencyFormat;

        @Builder
        public Money(double amount, Currency currency, Locale locale) {
            this.amount = amount;
            this.currency = currency;
            this.locale = locale;
        }

        public void updateAmount(double amount) {
            this.amount = amount;
        }

        public String getCurrencyFormat() {
            return NumberFormat.getCurrencyInstance(locale).format(this.amount);
        }
    }
}
