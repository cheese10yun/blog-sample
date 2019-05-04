# 객체를 안전하게 생성하자

[실무에서 Lombok 사용법](https://github.com/cheese10yun/blog-sample/tree/master/lombok)에서 기본적인 Lombk 사용법과 Builder 사용법을 간단하게 정리 한 내용을 먼저 참고하면 좋습니다.

JPA를 이용하면 엔티티 객체들을 Builder 기반으로 생성하는 것이 흔한 패턴입니다. 이러한 경우 Builder의 문제점들과 이것을 더욱 안전하게 사용하는 방법에 대해서 이야기해보겠습니다.


## Builder로 안전하게 생성하자

JPA 엔티티 객체들에 Builder 어노테이션을 이용해서 엔티티 객체를 Builder를 이용하는 것이 흔한 패턴입니다. 이 패턴의 장단점을 알아보고 더욱 안전하게 객체를 생성하는 방법을 소개하겠습니다.

Builder 패턴을 사용하면 다음과 같은 장점이 있습니다. 

1. 인자가 많을 경우 쉽고 안전하게 객체를 생성할 수 있습니다.
2. 인자의 순서와 상관없이 객체를 생성할 수 있습니다.
3. **적절한 책임을 이름에 부여하여 가독성을 높일 수 있습니다.**


단점은 Builder의 단점이라기보다는 사용하는 패턴의 단점입니다.

### Sample Code
```java
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

  @NotEmpty @Column(name = "bank_name", nullable = false)
  private String bankName;

  @NotEmpty @Column(name = "account_number", nullable = false)
  private String accountNumber;

  @NotEmpty @Column(name = "account_holder", nullable = false)
  private String accountHolder;

  // 불안전한 객채 생성 패턴
  @Builder
  public Account(String bankName, String accountNumber, String accountHolder) {
    this.bankName = bankName;
    this.accountNumber = accountNumber;
    this.accountHolder = accountHolder;
  }

  // 안전한 객채 생성 패턴
  @Builder
  public Account(String bankName, String accountNumber, String accountHolder) {
    Assert.hasText(bankName, "bankName must not be empty");
    Assert.hasText(accountNumber, "accountNumber must not be empty");
    Assert.hasText(accountHolder, "accountHolder must not be empty");

    this.bankName = bankName;
    this.accountNumber = accountNumber;
    this.accountHolder = accountHolder;
  }
}
```

우선 데이터베이스의 칼럼이 `not null`인 경우에는 대부분의 엔티티의 멤버실의 값도 null이면 안됩니다. 그 뜻은 해당 객체를 생성할 경우에도 동일합니다.


```java
Account account = Account.builder().build(); // 불안전한 객체 생성 패턴으로 생성했을 경우
```
account 객체에는 모든 멤버 필드의 값이 null로 지정됩니다. 이것은 애초에 account 객체를 의도한 것처럼 생성되지 않은 경우입니다. account 객체로 추가적인 작업을 진행하면 NPE가 발생하게 됩니다.


```java
Account account = Account.builder().build(); // 안전한 객체 생성 패턴으로 생성했을 경우
```

안전한 객체 생성 패턴으로 생성했을 경우는 객체 생성이 `Assert`으로 객체 생성이 진행되지 않습니다. 필요한 값이 없는 상태에서 객체를 생성하면 이후 작업에서 예외가 발생하게 됩니다. 그보다 객체가 필요한 값이 없는 경우에는 적절하게 Exception 발생시켜 흐름을 종료하는 게 좋다고 생각합니다. 이것은 우리가 컨트롤러에서 유효성 검사를 하는 이유와 동일합니다.

객체가 필요한 값이 없음에도 불과하고 이후 로직들을 진행하게 되면 더 비싼 비용이 발생합니다. 이미 트랜잭션이 시작했다거나, 해당 작업에 관련된 알림이 나갔다거나 등등이 있습니다.

**Builder를 이용해서 객체를 생성하더라도 필수 값의 경우에는 반드시 그에 맞는 방어 코드를 작성하는 것이 좋다고 생각합니다.**

```java
public class AccountTest {

  @Test(expected = IllegalArgumentException.class)
  public void Account_accountHolder_비어있으면_exception() {
    Account.builder()
        .accountHolder("")
        .accountNumber("110-22345-22345")
        .bankName("신한은행")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void Account_accountNumber_비어있으면_exception() {
    Account.builder()
        .accountHolder("홍길동")
        .accountNumber("")
        .bankName("신한은행")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void Account_bankName_비어있으면_exception() {
    Account.builder()
        .accountHolder("홍길동")
        .accountNumber("110-22345-22345")
        .bankName("")
        .build();
  }

  @Test
  public void Account_test() {
    final Account address = Account.builder()
        .accountHolder("홍길동")
        .accountNumber("110-22345-22345")
        .bankName("신한은행")
        .build();

    assertThat(address.getAccountHolder()).isEqualTo("홍길동");
    assertThat(address.getAccountNumber()).isEqualTo("110-22345-22345");
    assertThat(address.getBankName()).isEqualTo("신한은행");
  }

}
```
**너무 당연한 테스트 코드도 훗날 자신 또는 팀원에게 좋은 길잡이가 됩니다. 코드를 작성하면 반드시 테스트 코드를 작성하는 것이 좋습니다.**

주문이라는 객체는 반드시 제품이라는 객체가 1개 이상은 있어야 합니다.

```java
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private Address address;

  @OneToMany(mappedBy = "order")
  private List<Product> products = new ArrayList<>();

  @Builder
  public Order(Address address, List<Product> products) {
    Assert.notNull(address, "address must not be null");
    Assert.notNull(products, "products must not be null");
    Assert.notEmpty(products, "products must not be empty");

    this.address = address;
    this.products = products;
  }
}
```
위처럼 products 객체가 null 이거나 empty인 경우에는 Order 객체 생성을 못 하게 사전에 막아야 합니다. order 객체가 완전하지 않을 경우 추가적으로 발생하는 문제들도 많으며 테스트 코드 작성 시에도 어려움을 겪게 됩니다.


## Builder 이름으로 책임을 부여 하자

주문에 대한 환불이 있을 경우 환불에 대한 금액을 신용 카드 취소, 계좌 기반 환불이 있을 수 있습니다.

신용 카드 결제 취소일 경우에는 신용 카드 정보를 받아야 하고(실제 이런식으로 신용 카드 환불이 진행되지는 않습니다.), 계좌 정보를 입력받아야 하는 경우 하나의 Builder인 경우에는 필수 값 검증이 어렵습니다. 

신용 카드 정보와, 계좌 정보가 같이 넘어오면 어떻게 할 것인가? 이런 문제부터 생각할 것들이 많습니다. 이런 경우 아래와 같이 Builder의 이름을 명확하게 해서 책임을 부여하는 것이 좋습니다.


### Code
```java
@Entity
@Table(name = "refund")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Embedded
  private Account account;

  @Embedded
  private CreditCard creditCard;

  @OneToOne
  @JoinColumn(name = "order_id", nullable = false, updatable = false)
  private Order order;


  @Builder(builderClassName = "ByAccountBuilder", builderMethodName = "ByAccountBuilder") // 계좌 번호 기반 환불, Builder 이름을 부여해서 그에 따른 책임 부여, 그에 따른 필수 인자값 명확
  public Refund(Account account, Order order) {
    Assert.notNull(account, "account must not be null");
    Assert.notNull(order, "order must not be null");

    this.order = order;
    this.account = account;
  }

  @Builder(builderClassName = "ByCreditBuilder", builderMethodName = "ByCreditBuilder")  // 신용 카드 기반 환불, Builder 이름을 부여해서 그에 따른 책임 부여, 그에 따른 필수 인자값 명확
  public Refund(CreditCard creditCard, Order order) {
    Assert.notNull(creditCard, "creditCard must not be null");
    Assert.notNull(order, "order must not be null");

    this.order = order;
    this.creditCard = creditCard;
  }
}
```

신용카드 환불인 경우에는 신용카드 정보를 입력받게 하고, 계좌 환불인 경우에는 계좌 환불을 입력받게 합니다. **빌더의 이름으로 책임을 명확하게 부여하고, 받아야 하는 인자도 명확해지게 됩니다.**

```java

public class RefundTest {
    ...
    ...

  @Test
  public void ByAccountBuilder_test() {
    final Refund refund = Refund.ByAccountBuilder() // 빌더 이름으로 명확하게 그 의도를 드러 내고 있습니다.
        .account(account)
        .order(order)
        .build();

    assertThat(refund.getAccount()).isEqualTo(account);
    assertThat(refund.getOrder()).isEqualTo(order);
  }

  @Test
  public void ByCreditBuilder_test() {
    final Refund refund = Refund.ByCreditBuilder() // 빌더 이름으로 명확하게 그 의도를 드러 내고 있습니다.
        .creditCard(creditCard)
        .order(order)
        .build();

    assertThat(refund.getCreditCard()).isEqualTo(creditCard);
    assertThat(refund.getOrder()).isEqualTo(order);
  }

}
```

## 결론

필수 값임에도 불과하고 Builder에서 충분히 검사를 하지 않으면 에러의 발생은 뒷단으로 넘어가게 됩니다. 최악의 경우에는 데이터베이스 insert 시 해당 값이 not null인 경우 데이터베이스에서 에러를 발생시키게 됩니다. 
이렇게 되면 개발자에게 늦은 피드백을 주게 됩니다. 개발이 끝난 이후에 통합 테스트 구동 or 직접 스프링 구동해서 테스트 하는 방법은 에러에대한 피드백이 늦어지고 결국 생상성 저하로 이어진다고 생각합니다.

**가능하다면 POJO 기반으로 빠르게 코드상으로 피드백을 받는 것이 좋다고 생각하고 있습니다.**

