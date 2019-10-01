# Custom Validation 
스프링에서는 JSR 303 기반 어노테이션 기반으로 일관성 있는 Validation을 진행할 수 있습니다. 하지만 `@NotNull`, `@NotEmpty`, `@Email`과 같은 검증은 가능하지만 비즈니스적에 맞는 Validation은 별도로 진행해야 합니다.

예를 들어 주문에 대한 결제 정보를 받는 경우 카드 결제 시에는 카드 정보, 무통장 결제에는 계좌 정보를 입력받아야 합니다. 해당 요청을 JSON으로 표현하면 아래와 같습니다.

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

위 JSON 같이 `account`, `card` 값은 `paymentMethod`에 따라 선택적으로 필수 값이 변경되니 단순하게 `@NotNull`, `@NotEmpty` 어노테이션으로 검증하기가 어렵습니다. 이러한 경우 ConstraintValidator을 직접 구현해야 합니다.

## ConstraintValidator의 장점
ConstraintValidator을 직접 구현하지 않고도 다양한 방법으로 해당 Validation을 진행할 수 있지만 ConstraintValidator 기반으로 검증하는 게 다음과 같은 장점이 있다고 생각합니다.

### 일관성 있는 Validation 처리 방법
검증을 진행하는 방법과 시점이 매우 다양하다 보니 많은 개발자들이 만들어가는 프로젝트인 경우에 전체적인 코드의 통일성이 떨어지게 됩니다. Validation을 사용하면 검증 방법과, 검증 시점(어느 레이어에서 진행되는지)을 통일성 있게 가져갈 수 있습니다.

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
위와 같은 식으로 Controller 코드에서 검증을 진행하는 것도 방법이지만 이렇게 되면 다른 컨트롤러에서 위와 같은 중복 코드가 발생할 수 있고 이러한 중복 코드로 코드의 응집력이 떨어지게 되면 변경이 발생했을 때 해당 코드를 찾아가서 직접 수정해야 합니다. 직접 수정하는 것이 큰 리소스가 들지 않더라도 이는 실수하기 좋은 환경입니다.

이러한 컨트롤러에서의 중복 코드를 제거하기 위해서는 **컨트롤러 진입 직전 Interceptor에서 검증하는 것이 바람직합니다.** `ConstraintValidator` 기반으로 유효성 검증을 진행하면 검증 시점의(Interceptor에서 진행) 통일성을 갖습니다. 

### 일관성 있는 ErrorResponse
[스프링 가이드 - 통일된 Error Response 객체](https://github.com/cheese10yun/spring-guide/blob/master/docs/exception-guide.md#통일된-error-response-객체)에서 이야기했듯이 ErrorReponse는 항상 동일한 포맷으로 나 갸야 합니다. `ConstraintValidator`을 사용해서 `@Valid`을 기반으로 검증을 하면 `MethodArgumentNotValidException`이 발생하고 이 예외를 [@ControllerAdvice로 모든 예외를 핸들링](https://github.com/cheese10yun/spring-guide/blob/master/docs/exception-guide.md#controlleradvice로-모든-예외를-핸들링)에서 정리한 것처럼 통일성 있는 ErrorResponse 포맷으로 처리할 수 있습니다.

```
POST http://127.0.0.1:8080/order
Content-Type: application/json

{
  "price": 100.00,
  "payment": {
    "paymentMethod": "BANK_TRANSFER",
    "account": {
      "holder": "",
      "number": "",
      "bankCode": ""
    }
  },
  "address": {
    "city": "NOWON-GU",
    "state": "SEOUL",
    "zipCode": "09876?"
  }
}
```

위와 같이 요청을 보냈을 경우 아래처럼 Error Response를 갖습니다.


```json
{
  "message": "입력값이 올바르지 않습니다.",
  "status": 400,
  "errors": [
    {
      "field": "payment.account.bankCode",
      "value": "",
      "reason": "은행코드는 필수입니다."
    },
    {
      "field": "payment.account.number",
      "value": "",
      "reason": "계좌번호는 필수값입니다."
    },
    {
      "field": "payment.account.holder",
      "value": "",
      "reason": "계좌주는 값은 필수 입니다."
    }
  ],
  "code": "C001",
  "timestamp": "2019-09-21T23:21:26.006"
}
```

`ConstraintValidator`를 통해서 errors 배열로 올바르지 않은 필드에 대해서 리스트로 내려줌으로써 더 구체적인 에러를 응답하게할 수 있습니다. 

## ConstraintValidator 사용법

### Annotation 생성
```java
@Documented
@Constraint(validatedBy = OrderSheetFormValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderSheetForm {

    String message() default "Order sheet form is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
```

### 검증 로직이 있는 OrderSheetFormValidator 생성

```java
public class OrderSheetFormValidator implements ConstraintValidator<OrderSheetForm, OrderSheetRequest> { // (1)

    @Override
    public void initialize(OrderSheetForm constraintAnnotation) {

    }

    @Override
    public boolean isValid(OrderSheetRequest value, ConstraintValidatorContext context) {
        int invalidCount = 0; // (2)

        if (value.getPayment().hasPaymentInfo()) {
            addConstraintViolation(context, "카드 정보 혹은 계좌정보는 필수입니다.", "payment"); // (3)
            invalidCount += 1;
        }

        if (value.getPayment().getPaymentMethod() == PaymentMethod.CARD) {
            final Card card = value.getPayment().getCard();

            if (card == null) {
                addConstraintViolation(context, "카드 필수입니다.", "payment", "card");
            } else {
                if (StringUtils.isEmpty(card.getBrand())) {
                    addConstraintViolation(context, "카드 브렌드는 필수입니다.", "payment", "card", "brand");
                    invalidCount += 1;
                }
                if (StringUtils.isEmpty(card.getCsv())) {
                    addConstraintViolation(context, "CSV 값은 필수 입니다.", "payment", "card", "csv");
                    invalidCount += 1;
                }
                if (StringUtils.isEmpty(card.getNumber())) {
                    addConstraintViolation(context, "카드 번호는 필수 입니다.", "payment", "card", "number");
                    invalidCount += 1;
                }
            }
        }

        ...
        return invalidCount == 0; // (6)
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String errorMessage,
        String firstNode, String secondNode, String thirdNode) {
        context.disableDefaultConstraintViolation(); // (4)
        context.buildConstraintViolationWithTemplate(errorMessage) // (5)
            .addPropertyNode(firstNode)
            .addPropertyNode(secondNode)
            .addPropertyNode(thirdNode)
            .addConstraintViolation();
    }
}
```

* (1) ConstraintValidator<OrderSheetForm, OrderSheetRequest>를 상속받습니다. `OrderSheetForm` 작성한 위에서 생성한 어노테이션, `OrderSheetRequest`는 `@RequestBody`으로 받는 객체입니다.
* (2) `invalidCount`는 검증이 실패할 때마다 증가할 카운트 변수입니다.
* (3) `addConstraintViolation` 메서드를 통해서 에러 메시지와 검증한 node key 값을 넘겨줍니다. 해당 node는 ErrorResponse의 `errors[].field`에 바인딩 됩니다.
* (4) 해당 메서드로 `@OrderSheetForm`의 `default "Order sheet form is invalid";` 값을 disable 시킵니다. 
* (5) 해당 메서드로 검증에 대한 Violation 을 추가합니다.
* (6) `invalidCount == 0` 아닌 경우에는 `false`


### OrderSheetRequest

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@OrderSheetForm // (1)
public class OrderSheetRequest {

    @Min(1)
    private BigDecimal price;

    @NotNull
    @Valid // (2)
    private Payment payment;

    @NotNull
    @Valid
    private Address address;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @ToString // (3)
    public static class Payment {

        @NotNull
        private PaymentMethod paymentMethod;
        private Account account;
        private Card card;

        @JsonIgnore
        public boolean hasPaymentInfo() {
            return account != null && card != null;
        }

    }
    ...
}
```

* (1) `@OrderSheetForm`을 추가해서 `OrderSheetFormValidator`가 동작하게 합니다.
* (2) `@Valid`을 추가해서 각 클래스의 `JSR-303` 기반 어노테이션이 동작하게 합니다. `@Valid`이 없는 경우 payment.PaymentMethod의 `@NotNull` 동작하지 않습니다.
* (3) Error[].value 값이 객체인 경우에 해당 객체의 정보를 출력하기 위해서 `@ToString`을 추가합니다.

### Contoller

```java
@RestController
@RequestMapping("/order")
public class OrderApi {

    @PostMapping
    public OrderSheetRequest order(@RequestBody @Valid final OrderSheetRequest dto) {
        return dto;
    }
}
```
* `@Valid` 어노테이션으로 검증을 진행합니다.