# @Sql을 통해서 테스트 코드를 쉽게 작성하자

데이터베이스와 디펜던시가 있는 테스트를 진행하는 경우 given절에 해당하는 데이터를 set up한 이후에 로직에 대한 검증을 진행하는 것이 일반적입니다. 이때 given절에 대한 데이터 set up을 `*.sql` 파일 기반으로 작성하는 것입니다.


## @Sql 사용법

```sql
// /src/test/resources/payment-setup.sql
insert into payment (amount, created_at, order_id, updated_at)
values (1000, now(), 1, now()),
       (2000, now(), 2, now()),
       (3000, now(), 3, now()),
       (4000, now(), 4, now()),
       (5000, now(), 5, now()),
       (6000, now(), 6, now()),
       (7000, now(), 7, now()),
       (8000, now(), 8, now()),
       (9000, now(), 9, now()),
       (10000, now(), 10, now()),
       (11000, now(), 11, now()),
       (12000, now(), 12, now())
;
```

```kotlin
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SqlTest(
    private val paymentRepository: PaymentRepository
) {

    @AfterAll
    internal fun setUp() {
        println("BeforeAll: 모든 테스트 끝난 이후 ")
        println("payments size ${paymentRepository.findAll().size}")
    }

    @Sql("/payment-setup.sql")
    @Test
    fun `sql test code`() {
        //given

        //when
        val payments = paymentRepository.findAll().toList()

        //then
        then(payments).hasSize(12)
        println("sql test code")
    }
}
```

사용방법은 간단합니다. `@Sql` 어노테이션을 작성하고 해당 경로에 `*.sql` 파일을 위치 시킵니다.

![](img/test-result-1.png)

테스트 코드는 통과했고, AfterAll 메서드를 로그를 보면 `payments size 12`으로 롤백이 진행되지 않는것을 볼 수 있습니다. 테스트 클래스 상단에 `@Transactional` 어노테이션을 작성하면 자동롤백 처리가 됩니다. 한 번 테스트 해보겠습니다.

![](img/test-result-2.png)

`payments size 0` 으로 정상적으로 롤백된것 확인할 수 있습니다. 


## @SqlGroup 사용법

`@SqlGroup`은 여러 `@Sql` 집계하는 컨테이너 주석입니다.

하지만 `@Transactional`으로 롤백이 어려운 경우가 있습니다. 다표적으로 Spring Batch에서의 테스트코드 

## Todo
* 왜 SQL로 작성하는것이 편리한지?
* SQL기본 사용방법
* SQL Group 사용 방법