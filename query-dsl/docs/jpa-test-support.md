## JPA 기반 테스트 코드 작성 팁

JPA 테스트 코드를 작성할 불편한 부분이 있습니다. 제가 경험했던 불편한 부분들을 소개하고 해결 방법에대해서 포스팅 하려고 합니다.

## JPA 기반 테스트 불펴한 점들

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

물론 테스트 코드이기 떄문에 DI 받아야하는 항목들이 많아지는것이 상대적으로 문제가 크게 발생하지는 않지만 코드량이 많이 발생하는 문제가 있습니다. 


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

해당 서비스의 코드가 여러 enttiy의 여러 row의 변경을 가할 때 Then에서 검증을 진행할 때 문제가 발생하빈다. 해당 조회 코드가 없는 경우 **오직 테스트를 위해서만 조회용 코드를 일반 코드에 작성해야 합니다.**

그렇다면 **테스트 코드에서만 사용할 코드를 추가로 작성하거나 Test Scope에서 사용할 Repository를 떠로 작성해야합니다.**



## 해결 방법

```kotlin
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@ActiveProfiles("test")
abstract class SpringBootTestSupport {
    @Autowired
    protected lateinit var entityManager: EntityManager

    @Autowired
    protected lateinit var query: JPAQueryFactory

    protected fun <T> save(entity: T): T {
        entityManager.persist(entity)
        entityManager.flush()
        entityManager.clear()

        return entity
    }

    protected fun <T> saveAll(entities: Iterable<T>): Iterable<T> {
        for (entity in entities) {
            entityManager.persist(entity)
        }

        entityManager.flush()
        entityManager.clear()

        return entities
    }

    protected fun deleteAll(qEntity: EntityPathBase<*>) {
        query.delete(qEntity).execute()

        entityManager.flush()
        entityManager.clear()
    }
}
```
해결 방법은 테스트 코드를 위해서 필요한 기능을 제공해주는 `SpringBootTestSupport`을 제공해주는 것입니다. 자세한 코드는 아래에서 설명하겠습니다.

### 과도한 Dependency

```kotlin
protected fun <T> save(entity: T): T {
    entityManager.persist(entity)
    entityManager.flush()
    entityManager.clear()

    return entity
}

protected fun <T> saveAll(entities: Iterable<T>): Iterable<T> {
    for (entity in entities) {
        entityManager.persist(entity)
    }

    entityManager.flush()
    entityManager.clear()

    return entities
}
protected fun deleteAll(qEntity: EntityPathBase<*>) {
    query.delete(qEntity).execute()

    entityManager.flush()
    entityManager.clear()
}
```
`EnttiyManager`를 기반으로 persist 작업을 진행할 수 있습니다. 그렇게 되면 굳이 Repository 의존성이 필요하지 않고 `SpringBootTestSupport`에서 DI 받은 `EnttiyManager`을 이용해서 영속화 작업을 진행하면 편리하게 테스트 코드를 작성할 수 있습니다.

주의해야할 점은

```kotlin
...
entityManager.flush()
entityManager.clear()
```
flush, clear를 강제로 발생시키고 있는 점입니다. 









## ???
