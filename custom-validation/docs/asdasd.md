# Custom Validation 

스프링에서는 JSR 303 기반어노테이션 기반으로 일관성있는 Validation을 진행할 수 있습니다. 하지만 `@NotNull`, `@NotEmpty`, `@Email`과 같은 검증은 가능하지만 비지니스적에 맞는 Validation은 별도로 진행해야합니다.

예를들어 주문에 대한결제 정보를 받는 경우 카드 결제시에는 카드 정보, 무통장 결제에는 계좌정보를 입력받아야합니다. 해당 요청을 JSON으로 표현 하면 아래와 같습니다.

## 무통장 결제
```json
{
  "price": 100.00,
  "payment": {
    "paymentMethod": "BANK_TRANSFER",
    "account": {
      "number": "110-202034-2234",
      "bankCode": "2003",
      "holder" : "홍길동"
    }
  },
  "address": {
    "city": "NOWON-GU",
    "state": "SEOUL",
    "zipCode": "09876?"
  }
}
```

## 카드 결제
```json
{
  "price": 100.00,
  "payment": {
    "paymentMethod": "CARD",
    "card": {
      "number": "25523-22394",
      "brand": "323",
      "csv" : "삼성카드"
    }
  },
  "address": {
    "city": "NOWON-GU",
    "state": "SEOUL",
    "zipCode": "09876?"
  }
}
```

위 JSON 같이 `account`, `card` 값은 `paymentMethod`에 따라 선택적으로 필수 값이 변경되니 단순하게 `@NotNull`, `@NotEmpty` 어노테이션으로 검증하기가 어렵습니다. 이러한 경우 ConstraintValidator을 직접 구현 해야합니다.

## ConstraintValidator의 장점
ConstraintValidator을 직접 구현하지 않고도 다양한 방법으로 해당 Validation을 진행할 수 있지만 ConstraintValidator 기반으로 검증하는게 다음과 같은 장점이 있다고 생각합니다.

### 일관성있는 Validation 처리 방법
검증을 진행하는 방법이과 시점이 매우 다양하다보니 많은 개발자들이 만들어가는 프로젝트인 경우에 전체적인 코드의 통일성이 떨어지게됩니다. 특히 검증을 시작하는 시점이 Controller 영역에서 진행하는 것이 좋습니다. 

```java
@PostMapping
    public OrderSheetRequest order(@RequestBody @Valid final OrderSheetRequest dto) {

        if (dto.getPayment().getPaymentMethod() == PaymentMethod.BANK_TRANSFER)) {
            // 계좌정보가 제대로 넘어 왔는지 검증
        }
        
        if((dto.getPayment().getPaymentMethod() == PaymentMethod.CARD)){
            // 카드 정보 제대로 넘어 왔는지 검증
        }
        
        return dto;
    }
```

위와 같은 식으로 Controller 코드에서 검증을 진행하는 것도 방법이지만 이렇게되면 다른 컨트롤러에서 위와 같은 중복 코드가 발생할수 있고 이러한 중보 코드로 코드의 응집력이 떨어지게 되면 변경이 발생했을때 해당 코드를 찾아가서 직접 수정해야 합니다. 직접 수정하는 것이 큰 리소스가 들지 않더라도 이는 실수하기 좋은 환경입니다.

**해당 컨트롤러 진입 직전 Interceptord에서 검증하는 것이 바람직합니다.** `ConstraintValidator`에서 진행하면 검증 방법과 검증 시점이(Interceptord에서 진행) 통일성을 갖습니다. 

### 일관성 있는 ErrorResponse
[스프링 가이드 - 통일된 Error Response 객체](https://github.com/cheese10yun/spring-guide/blob/master/docs/exception-guide.md#통일된-error-response-객체)에서 이야기 했듯이 ErrorReponse는 항상 동일한 포멧으로 나갸야합니다. `ConstraintValidator`을 사용해서 `@Valid`을 기반으로 검증을하면 `MethodArgumentNotValidException`이 발생하고 이 예외를 [@ControllerAdvice로 모든 예외를 핸들링](https://github.com/cheese10yun/spring-guide/blob/master/docs/exception-guide.md#controlleradvice로-모든-예외를-핸들링)에서 정리한 것 처럼 통일성 있는 ErrorResponse 포멧으로 처리할 수 있습니다. 