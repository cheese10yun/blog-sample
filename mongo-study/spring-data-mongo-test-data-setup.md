# Spring Data MongoDB Test Data 손쉽게 셋업하기

테스트 코드의 가치가 널리 인정받으며, 이제 그 필요성을 언급하는 것은 의미가 없어졌습니다. 테스트 코드의 가장 큰 매력은 바로 구현 코드에 대한 실시간 피드백을 제공하고, 이를 바탕으로 구현 코드를 지속적으로 개선해 나갈 수 있다는 점입니다. 이 개념은 "[실무에서 적용하는 테스트 코드 작성 방법과 노하우 Part 2: 테스트 코드로부터 피드백 받기](https://tech.kakaopay.com/post/mock-test-code-part-2/)"사내 기술 블로그에 이미 다룬 바 있습니다. 폭넓은 테스트를 작성하고 실행하기 위해서는 테스트 코드의 간편한 작성이 필수적입니다. 본 포스팅에서는 Spring Data MongoDB를 사용하여 Given 절의 데이터 셋업을 쉽게 하는 방법을 소개할 것입니다.

## 데이터 셋업의 어려움과 중요성

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/intellij-test/intellij-test/images/layer-4.png)
위 이미지 처럼 주문 테스트 코드를 작성하기 위해서는 다양한 데이터를 셋업하는 것은 필수적입니다. 테스트 셋업이 복잡하면 테스트 범위가 좁아지고, 품질이 낮아질 수 있는 피드백을 받게 됩니다. 따라서, 테스트 코드를 쉽게 작성하고 다양한 시나리오를 손쉽게 검증할 수 있는 환경을 만드는 것이 중요합니다. RDBMS에서는 @Sql 어노테이션을 이용해 테스트 데이터를 간단히 셋업할 수 있으며, 이를 통해 테스트 케이스를 원활하게 확장할 수 있습니다. 이에 관한 자세한 방법은 [Sql을 통해서 테스트 코드를 쉽게 작성하자" 포스팅에서 설명하고 있습니다](https://cheese10yun.github.io/sql-test/). 이 자료는 Sql을 활용하여 다양한 테스트 데이터를 쉽게 구성하는 방법을 제공합니다.

## 손쉽게 데이터 셋업 하는 방법

```kotlin
@SqlGroup(
    Sql(
        value = ["/schema.sql", "/order-setup.sql", "/coupon.sql", "product.sql"],
        config = SqlConfig(
            dataSource = "dataSource",
            transactionManager = "transactionManager"
        ),
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    ),
    Sql(
        value = ["/delete.sql"],
        config = SqlConfig(
            dataSource = "dataSource",
            transactionManager = "transactionManager"
        ),
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
)
@Test
fun `sql test code`() {
    // test code
}
```

`@SqlGroup` 어노테이션을 사용하면 `*.sql` 파일을 통해 테스트 데이터를 쉽게 준비할 수 있습니다. 이렇게 데이터를 만들면 setter를 막아 데이터 변경 단위를 논리적으로 제공하는 경우라면 큰 장점이 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/intellij-test/intellij-test/images/order-flow.003.jpeg)

1. Order라는 엔티티 객체를 테스트 코드를 작성하려면 특정 Snapshot 상태로 만들어야 한다.
2. 테스트 코드를 작성하는 구간은 상품 준비 -> 배송시작 임으로 해당 객체를 상품 준비 상태로 만들어야 한다.
3. 하지만 단순 setter가 없기 때문에 상품 준비 중 객체로 직접 만드는 것이 어려운 부분이 있다.

**이런 경우 `*.sql` 파일을 이용하면 setter가 없어도 테스트 데이터를 비즈니스 제약 없이 쉽게 준비할 수 있습니다.**

## MongoDB용 XXX

위 `@Sql` 어노테이션 처럼 테스트 셋업을 제공 해주는 기능이 없기 떄문에 직접 만들어야 합니다.

위 코드 처럼 순차적으로 `.sql` 파일을 기반으로

이번 포스팅에서는 Spring Data MongoDB를 사용하는 테스트 코드를 보다 쉽게

Given절에 해당하는 데이

블로그 포스팅을 위한 전반적인 내용 구성을 위해 다음과 같이 정리할 수 있습니다:

1. **서론**: Spring Data MongoDB를 사용하여 테스트 환경을 구성하는 현재의 어려움을 소개하고, 이를 해결할 수 있는 새로운 방법에 대한 필요성을 강조합니다. @Sql 어노테이션을 사용하여 SQL 데이터베이스를 테스트하는 Spring 테스트의 편리함을 언급하며, MongoDB 환경에서 비슷한 접근 방식의 부재를 지적합니다.

2. **문제점 해설**: MongoDB의 임베디드 객체 사용으로 인한 데이터 설정의 복잡성에 대해 설명합니다. 객체를 수동으로 생성하여 given 데이터를 만들어내는 과정에서 발생하는 어려움과 시간 소모를 상세히 기술합니다.

3. **솔루션 설명**: 사용자 정의 어노테이션을 통해 JSON 파일을 기반으로 데이터를 설정하는 방법을 소개합니다. 이 접근법이 어떻게 기존 문제를 해결하는지, 테스트 데이터 준비 과정을 단순화하는 방법과 그 장점에 대해 설명합니다.

4. **장점 강조**: JSON을 이용한 데이터 셋업의 여러 장점에 대해 구체적으로 강조합니다. 예를 들어, setter가 없는 객체의 데이터 셋업이 어려울 때 JSON을 이용하면 이를 어떻게 간단히 해결할 수 있는지 실제 사례를 들어 설명합니다.

5. **실제 사용 예**: 실제 코드 예시나 테스트 케이스를 통해 어노테이션의 사용 방법을 보여주고, 여러 데이터를 조합하여 복잡한 테스트 케이스를 쉽게 구성하는 방법을 시연합니다.

6. **결론**: 사용자 정의 어노테이션을 사용하는 방법이 테스트 데이터 준비를 어떻게 개선하는지 요약하고, 이를 통해 개발자들이 시간을 절약하고 더 효율적으로 작업할 수 있게 되는 이점을 다시 한번 강조합니다.

이 구성을 바탕으로 블로그 포스팅을 작성하면, 독자들이 어떻게 Spring Data MongoDB 테스트를 향상시킬 수 있는지 명확하게 이해할 수 있을 것입니다.