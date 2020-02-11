---
layout: post
title: Spring OOP 프로그래밍 예제(2)
subtitle: 은행 API를 통한 환율 계산기
catalog: true
header-img: 'https://i.imgur.com/avC1Xor.jpg'
tags:
  - Spring
  - OOP
thumbnail: 'https://i.imgur.com/nquQoeh.png'
date: 2018-03-30 00:00:00
---

![](https://i.imgur.com/nquQoeh.png)

## 요구사항

* 해외 송금에 필요한 계산기 기능
* 미국 USD 에서 대한민국 KRW 로 계산 기능
    - ex -> $500 -> ₩539,337
    - 미국 -> 한국 환율을 정보는 BankOfAmerica 은행사 API를 사용 해야한다
* 대한민국 KRW 에서 미국 USD 로 계산 기능
    - ₩500,000 - > $463.43
    - 한국 -> 미국 환율을 정보는 신한 은행사 API를 사용 해야한다.

## 도메인
* 보내는 곳 , 받는 곳 이 있다고 생각하고 도메인을 생각 했습니다.

### Remittance : 송금
* 금액을 송금합니다.
* 금액을 송금 하기 위해서 보내는 **금액**, 보내는 금액의 **통화**, 보내는**나라**는 필수 입니다.

### Deposit : 입금
* 금액을 입금 받습니다.
* 금액을 입금 받기 하기 위해서 받는 **금액**, 받는 금액의 **통화**, 받는**나라**는 필수 입니다.

### 공통 키워드
* ... 위해서 받는 **금액**, 받는 금액의 **통화**, 받는**나라**는 필수 입니다.

### Money
```java
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
    ...
    ...
}
```
* 공통 키워드를 만족시키는 클래스를 작성 헸습니다.
    - 받는 **금액** : `amount`, 받는 금액의 **통화** : `currency`,  받는**나라** : `locale`
* 돈을 받는 곳, 보내는 곳 모든곳에서 사용하는 **자료형(클래스)로 만들어 재사용 성을높일 수 있습니다.**
* 아래 코드 보다 확실히 비즈니스를 이해하기 쉽고 관리하기 편합니다.

```java
private double sendMoney;
private double receiveMoney;
private Currency sendCurrency;
private Currency receiveCurrency;
private Locale sendLocale;
private Locale receiveLocale;
```

### Transaction

```java
public static class Transaction {
    private Remittance remittance;
    private Deposit deposit;
    ...
}

public static class Remittance {
    private Money money;
    ...
}

public static class Deposit {
    private Money money;
    ...
}
public static class Money {
    private double amount;
    private Currency currency;
    private Locale locale;
    private String amountCurrencyFormat;
    ...
}
```
* 거래를 하기 위해서 송금, 입금이 필요 하다는 것을 쉽게 알 수 있습니다.
* 송금, 입금을 하기 위해서는 Money라는 타입이 필요합니다.
* Money 타입에는 거래를 하기 위한 필요 데이터들이 모여 있습니다.

## 요구사항 구현
도메인도 작업이 어느정도 완료됬으니 요구사항에 필요한 기능을 개발해 보겠습니다.


### BankOfAmericaApi
```java
public class BankOfAmericaApi {
    public ApiCommonDto.ExchangeRate consumeExchangeRate() {

        //실제 은행사 API 호출해서 환율 정보를 가져오는 코드가 작성 됩니다.
        return ApiCommonDto.ExchangeRate.builder()
                .rate(1059.999963)
                .corridor("US_TO_KOR")
                .corridor("BANK_OF_AMERICA")
                .build();
    }
}
```
* Bank Of America 은행사 API 호출을 담당하는 클래스입니다. 이 클래스를 통해서 KRW -> USD 환율 정보를 가져옵니다.

### ShinhanApi.class
```java
public class ShinhanApi {
    public ApiCommonDto.ExchangeRate consumeExchangeRate() {
        //실제 은행사 API 호출해서 환율 정보를 가져오는 코드가 작성 됩니다.
        return ApiCommonDto.ExchangeRate.builder()
                .rate(0.000943)
                .corridor("KOR_TO_US")
                .company("Shinhan")
                .build();
    }
}
```
* 신한 은행사 API 호출을 담당하는 클래스입니다. 이 클래스를 통해서 통해서  USD -> KRW 환율  정보를 가져옵니다.


### ExchangeRate
```java
public interface ExchangeRate {
    ApiCommonDto.ExchangeRate getExchangeRate(CalculatorDto.Transaction transaction);
}
```

* 환율 정보를 가져오는 것을 추상화 시킨 인터페이스입니다.
* `getExchangeRate` 추상화 메소드를 통해서 하위의 세부 구현체에서 구현하게 됩니다.

### ShinhanExchangeRate
```java
public class ShinhanExchangeRate implements ExchangeRate {
    ...
    @Override
    public ApiCommonDto.ExchangeRate getExchangeRate(CalculatorDto.Transaction transaction) {
        return shinhanApi.consumeExchangeRate();
    }
    ...
}

public class BankOfAmericaExchangeRate implements ExchangeRate {
    ...
    @Override
    public ApiCommonDto.ExchangeRate getExchangeRate(CalculatorDto.Transaction transaction) {
        return bankOfAmericaApi.consumeExchangeRate();
    }
    ...
}
```
* ShinhanApi, BankOfAmericaApi 클래스를 이용해서 환율 정보를 가져옵니다.(실제 인행사를 호출하는 코드가 아닙니다. 그냥 하드코딩된 값을 리턴합니다.)
* ExchangeRate 인터페이스를 구현하고 있습니다. 이 것을 통해서 IoC 효과를 갖을 수 있습니다.
* **처음에는 BankOfAmericaApi, ShinhanApi 클래스들을 인터페이스를 통해서 묶으려고 했습니다. 하지만 그것은 잘못된 설계라고 생각합니다. 객체는 자율적인 책임을 져야 하는데 인터페이스로 묶으면 객체들의 자율적인 책임을 방해하게 됩니다. 그 이유는 인터페이스의 추상화 메소드로 인해서 리턴해야할 값과 메게변수로 값이 고정됩니다. 이렇게 고정되면 은행사마다 API 호출 시 인증에 필요한 값, 넘겨야 할 데이터 등등 이 다를 수밖에 없는데 이것을 추상화시킨다는 것 자체가 바람직하지 않습니다. 예를 들어 다른 은행사의 API가 추가되면 또 그때 추상화(리턴 타입의 변경, 매개변수 변경)가 다시 요구됩니다.**



## Calculator
```java
public class Calculator {
    ...
    public CalculatorDto.Res calculate(CalculatorDto.Transaction transaction) {
        final ExchangeRate exchangeRate = getInstanceByLocale(transaction); //의존성 주입
        final double remittanceAmount = transaction.getRemittance().getMoney().getAmount();
        final double rate = exchangeRate.getExchangeRate(transaction).getRate(); //주입받은 의존성으로 파트너 은행사의 환율 정보 가져옴

        calculateDepositAmount(transaction.getDeposit(), remittanceAmount, rate); // 환율 정보 기반으로 입금액 계산
        ...
    }
    ...
    private ExchangeRate getInstanceByLocale(CalculatorDto.Transaction transaction) {
        final Locale locale = transaction.getRemittance().getMoney().getLocale();
        return exchangeRateFactory.getInstanceByLocale(locale);
    }
    ...
}
```

* 실제로 환율 정보를 계산을 담당하는 클래스입니다.
* `getInstanceByLocale` 메소드를 통해서 보내는 국가가 어디냐에 따라서 `ExchangeRate`에 알맞는 은행 API가 의존성 주입됩니다.
* 은행이 추가되더라도 `getInstanceByLocale` 메서드에 의존성만 추가해주면 **OCP**를 준수하는 코드가 됩니다.
* 이렇게 IoC를 이용해서 의존성 주입이 일어나느 것이 확장 및 유지보수에도 엄청난 장점이 있습니다. 이런 코드가 없다면 if, if, if 이 지속적으로 추가되며 앞에 작성된 if문을 이해하고 알맞는 위치에 또 if문을 추가해야 하는 악순환이 시작됩니다.


## USD -> KOR 계산

### Request
```curl
curl -X GET \
  'http://localhost:8080/calculator?remittanceAmount=1000&remittanceCurrency=USD&remittanceLocal=US&depositAmount=0&depositCurrency=KRW&depositLocal=KR' \
  -H 'cache-control: no-cache' \
  -H 'postman-token: a1e724a0-0ec9-195b-b744-221b3f238c3b'
```

### Response
```json
{
  "remittance": {
    "money": {
      "amount": 1000,
      "currency": "USD",
      "locale": "en_US",
      "amountCurrencyFormat": "$1,000.00"
    }
  },
  "deposit": {
    "money": {
      "amount": 1059999.963,
      "currency": "KRW",
      "locale": "ko_KR",
      "amountCurrencyFormat": "￦1,060,000"
    }
  },
  "exchangeRate": {
    "value": 1059.999963
  }
}
```

## KRW -> USD 계산

### Request
```
curl -X GET \
  'http://localhost:8080/calculator?remittanceAmount=1000000&remittanceCurrency=KRW&remittanceLocal=KR&depositAmount=0&depositCurrency=USD&depositLocal=US' \
  -H 'cache-control: no-cache' \
  -H 'postman-token: 33800725-5db0-1eca-926b-e0269da9d28c'
```

### Response
```json
{
    "remittance": {
        "money": {
            "amount": 1000000,
            "currency": "KRW",
            "locale": "ko_KR",
            "amountCurrencyFormat": "￦1,000,000"
        }
    },
    "deposit": {
        "money": {
            "amount": 943,
            "currency": "USD",
            "locale": "en_US",
            "amountCurrencyFormat": "$943.00"
        }
    },
    "exchangeRate": {
        "value": 0.000943
    }
}
```

