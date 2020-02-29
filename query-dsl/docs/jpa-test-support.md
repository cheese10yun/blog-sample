## JPA 기반 테스트 코드 작성 팁

JPA 테스트 코드를 작성할 불편한 부분이 있습니다. 제가 경험했던 불편한 부분들을 소개하고 해결 방법에대해서 포스팅 하려고 합니다.

## JPA 기반 테스트 불편한 점

### 과도한 Dependency

```kotlin
internal class JpaTest(
    private val paymentRepository: PaymentRepository,
    private val memberRepository: MemberRepository,
    private val orderRepository: OrderRepository
) : SpringBootTestSupport() {

    @Test
    internal fun `특정 테스트를 하기위해서는 외부 dependency가 필요하다`() {
        // 특정 테스트를 하기 위해서 많은 디펜던시가 필요하다.
        paymentRepository.save(Payment(BigDecimal.TEN))
        memberRepository.save(Member("username", 10, Team("team-name")))
        orderRepository.save(Order(BigDecimal.TEN))
    }
}
```
많은 디펜던시가 필요한 부분의 테스트 코드를 작성할 떄는 많은 Repository를 주입 받아서 테스트를 진헹 해야합니다. 위 코드처럼 특정 구간의 서비스를 테스트하기 위해서는 Given절을 작성할 때 흔하게 발생합니다.

물론 테스트 코드이기 떄문에 DI 받아야하는 항목들이 많아지는것이 상대적으로 문제가 크게 발생하지는 않지만 코드영이 많이 발생하는 문제가 있습니다.

### 검증이 필요한데 ?... 
무엇보다 큰 문제가 해당 테스트의 Then절에 있습니다. 

```kotlin
@Test
internal fun `특정 테스트를 하기위해서는 외부 dependency가 필요하다`() {
    // 특정 테스트를 하기 위해서 많은 디펜던시가 필요하다.
    paymentRepository.save(Payment(BigDecimal.TEN))
    memberRepository.save(Member("username", 10, Team("team-name")))
    orderRepository.save(Order(BigDecimal.TEN))


    // 특정 서비스가 여러 entity rows를 변경할때 아래와 같은 조회로 Then 이어가야 합니다.
    // paymentRepository.findBy... epository 메서드는 없는데??...
    // memberRepository.findBy...
    // orderRepository.findBy...
}
```
해당 서비스의 코드가 여러 enttiy의 여러 row의 변경을 가할 때 Then에서 검증을 진행할 때 문제가 발생합니다. 해당 조회 코드가 없는 경우 **오직 테스트를 위해서만 조회용 코드를 일반 코드에 작성해야 합니다.**

그렇다면 **테스트 코드에서만 사용할 코드를 추가로 작성하거나 Test Scope에서 사용할 Repository를 떠로 작성해야 합니다.**

## 해결 방법

```kotlin
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
abstract class SpringBootTestSupport {

    @Autowired
    protected lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    protected lateinit var query: JPAQueryFactory

    protected val entityManager: EntityManager by lazy {
        entityManagerFactory.createEntityManager()
    }

    protected val transaction: EntityTransaction by lazy {
        entityManager.transaction
    }

    protected fun <T> save(entity: T): T {
        transaction.begin()

        try {
            entityManager.persist(entity)
            entityManager.flush() // transaction commit시 자동으로 flush 발생시키나 명시적으로 선언
            transaction.commit()
            entityManager.clear()

        } catch (e: Exception) {
            transaction.rollback()
        }

        return entity
    }


    protected fun <T> saveAll(entities: Iterable<T>): Iterable<T> {
        transaction.begin()

        for (entity in entities) {
            try {
                entityManager.persist(entity)
                entityManager.flush() // transaction commit시 자동으로 flush 발생시키나 명시적으로 선언
                transaction.commit()
                entityManager.clear()

            } catch (e: Exception) {
                transaction.rollback()
            }
        }
        return entities
    }
}
```
해결 방법은 테스트 코드를 위해서 필요한 기능을 제공해 주는 `SpringBootTestSupport`을 제공해 주는 것입니다. 자세한 코드는 아래에서 설명하겠습니다.

### 과도한 Dependency 해결 방법

```kotlin
protected fun <T> save(entity: T): T {
    transaction.begin()

    try {
        entityManager.persist(entity)
        entityManager.flush() // transaction commit시 자동으로 flush 발생시키나 명시적으로 선언
        transaction.commit()
        entityManager.clear()
    } catch (e: Exception) {
        transaction.rollback()
    }
    return entity
}


protected fun <T> saveAll(entities: Iterable<T>): Iterable<T> {
    transaction.begin()

    for (entity in entities) {
        try {
            entityManager.persist(entity)
            entityManager.flush() // transaction commit시 자동으로 flush 발생시키나 명시적으로 선언
            transaction.commit()
            entityManager.clear()

        } catch (e: Exception) {
            transaction.rollback()
        }
    }
    return entities
}
```
`Given`절에서 작성하는 데이터 Set up이기 때문에 **트랜잭션을 완전히 분리하기 위해서 transactionn을 commit을 직접 수행 시킵니다.** transaction commit 시에 flush가 동작하지만 명시적으로 코드를 작성했습니다.


주의해야할 점은

```kotlin
entityManager.clear()
```
**EntityManager clear()을 강제로 발생시키는 있는 점입니다.**

![](https://github.com/cheese10yun/blog-sample/raw/master/query-dsl/docs/images/query-result-.png)

영속성 컨텍스트는 1차 캐시를 우선시 합니다. 즉 추가적인 JPQL 쿼리로 조회한 값이 영속성 컨텍스트에 존재하는 경우(식별자 값으로 동일성을 판단함) JPQL로 조회한 데이터를 버립니다. 즉 1차 캐시된것을 우선시합니다. 

그러기 때문에 EntityManager를 이용해서 영속성 컨텍스트를 초기화하는 것입니다. 자세한 내용은 [JPA 벌크 작업 주의점 - 영속성 컨텍스트는 1차 캐시된 것이 우선이다.](https://cheese10yun.github.io/jpa-bulk/#1)에 정리되어있습니다.

그리고 Give절에 작성하는 데이터는 이미 데이터베이스에 영송화되어 있다는 개념으로 테스트하는 것이니 이런 문제가 없더라도 최대한 동일한 환경을 구성해주는 것이 좋다고 생각합니다.

```kotlin
@Transactional
internal class JpaTestSupport : SpringBootTestSupport() {

    @Test
    internal fun `entityManager를 이용해서 dependency가 최소화 `() {
        // 특정 테스트를 하기 위해서 많은 디펜던시가 필요하다.
        save(Payment(BigDecimal.TEN))
        save(Member("username", 10, save(Team("team-ename"))))
        save(Coupon(BigDecimal.TEN))
    }
}
```
Entity의 영속성을 EnttiyManager를 통해서 진행하기 때문에 단순 save를 위해서 DI 받는 Repository가 없어졌습니다. 


### 검증이 필요한데 ?... 해결방법
```kotlin
@Configuration
class Configuration {

    @Bean
    fun query(entityManager: EntityManager): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }
}

...
abstract class SpringBootTestSupport {
    ...
    @Autowired
    protected lateinit var query: JPAQueryFactory
}
```
`JPAQueryFactory`을 `query`으로 Bean 등록을 진행합니다. Test Scope에서만 사용하는 것을 원하는 경우에는 Test directory에 `@TestConfiguration`으로 지정해도 됩니다.

`query`을 이 옹해서 테스트 코드 검증을 진행할 수 있습니다.

```kotlin
@Transactional
internal class JpaTestSupport : SpringBootTestSupport() {

    @Test
    internal fun `entityManager를 이용해서 dependency가 최소화 `() {
        // 특정 테스트를 하기 위해서 많은 디펜던시가 필요하다.
        save(Payment(BigDecimal.TEN))
        save(Member("username", 10, save(Team("team-ename"))))
        save(Coupon(BigDecimal.TEN))

        // 특정 서비스가 여러 entity rows를 변경할때 아래와 같은 조회로 Then 이어가야 합니다.
        // paymentRepository.findBy... epository 메서드는 없는데??...
        // memberRepository.findBy...
        // couponRepository.findBy...

        val payments = query.selectFrom(QPayment.payment)
            .where(QPayment.payment.amount.gt(BigDecimal.TEN))
            .fetch()

        val members = query.select(QMember.member.age)
            .from(QMember.member)
            .where(QMember.member.age.gt(20))
            .fetch()

        val coupons = query.selectFrom(QCoupon.coupon)
            .where(QCoupon.coupon.amount.eq(123.toBigDecimal()))
            .fetch()
    }
}
```
여러 엔티티의 여러 row의 수정을 진행했을 경우 해당 엔티티의 변경을 확인하기 위한 검증이 필요합니다. 이때 조회용 메서드를 단순히 테스트 코드에서만 사용하기 위해서 작성하기 위해서 작성하거나 Test Scope에 별도의 Repository를 구성해야 했지만 이제는 위에서 등록한 `query`을 이용해서 해당 비즈니스에 맞는 쿼리를 작성할 수 있습니다.


```kotlin
internal class PaymentServiceTest(
    private val paymentService: PaymentService
) : SpringBootTestSupport() {

    @Test
    internal fun `paymentZero test`() {
        //given
        val targetAmount = 105.toBigDecimal()
        saveAll((1..100).map {
            Payment(it.toBigDecimal().plus(BigDecimal.TEN).setScale(0))
        })

        //when
        paymentService.paymentZero(targetAmount)

        //then
        val count = query
            .selectFrom(qPayment)
            .where(qPayment.amount.gt(targetAmount))
            .fetchCount()

        then(count).isEqualTo(0)
    }
```
이런 식으로 심플하게 테스트 코드를 작성할 수 있습니다.

## 마무리
테스트 코드의 중요성은 강조하는 것이 의미 없을 정도로 현업에서 자리 잡았다고 생각합니다. 이만큼 중요성이 있는 부분이니 테스트 코드를 작성하기 편한 방법도 많이 연두되고 생각했으면 좋겠다는 생각으로 해당 포스팅을 정리했습니다.