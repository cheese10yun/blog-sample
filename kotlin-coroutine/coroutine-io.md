# Kotlin 코루틴 Dispatchers.IO: 특징과 활용 가이드

## 서론

Kotlin 코루틴은 복잡한 스레드 관리와 동시성 문제를 간단하게 해결할 수 있는 강력한 도구입니다. 이 글에서는 특히 I/O 작업에 최적화된 Dispatchers.IO에 집중하여, 이 디스패처가 어떻게 스레드 블록킹 문제를 해결하고 전체 애플리케이션의 성능과 반응성을 개선할 수 있는지 구체적인 코드 예제와 실행 로그를 통해 살펴봅니다. 또한, JDBC 드라이버나 파일 입출력, 동기식 네트워크 호출 등 블록킹이 발생할 수 있는 실제 상황에서 Dispatchers.IO를 적용하는 방법과 그 효과를 자세히 설명하여, 개발자가 실제 프로젝트에서 효율적인 비동기 처리를 구현할 수 있도록 돕는 것을 주요 목적으로 합니다.

## Dispatchers 종류 비교

아래 테이블은 각 디스패처의 특징과 사용 용도를 간략하게 정리한 것입니다.

| **디스패처**               | **설명**                                            | **사용 시나리오**                        | **실행 스레드 예시**                                |
|------------------------|---------------------------------------------------|------------------------------------|----------------------------------------------|
| Dispatchers.Main       | UI 스레드에서 실행되며, 메인 스레드 업데이트를 담당합니다.                | UI 업데이트, 사용자 이벤트 처리                | Android의 메인 스레드 등                            |
| Dispatchers.Default    | CPU 집약적인 작업에 적합하며, 공용 스레드 풀을 사용합니다.               | 복잡한 계산, 데이터 처리                     | 일반적인 백그라운드 작업 스레드                            |
| Dispatchers.Unconfined | 호출한 컨텍스트에 묶이지 않고, 일시적으로 다른 스레드에서 실행될 수 있습니다.      | 빠른 초기 작업, 스레드 전환 없이 동작 확인 시        | 컨텍스트에 따라 달라짐                                 |
| Dispatchers.IO         | I/O 작업에 최적화된 별도의 스레드 풀을 사용하며, 블록킹 작업에 대응할 수 있습니다. | 파일 I/O, 네트워크, DB 접근 등 블록킹이 발생하는 작업 | "DefaultDispatcher-worker-#" 와 같은 I/O 전용 스레드 |

Dispatchers.IO는 특히 I/O 작업에서 스레드가 블록킹(blocking) 되는 상황에 적합하여, 별도의 스레드 풀을 사용함으로써 전체 애플리케이션의 응답성을 유지하는 데 큰 역할을 합니다.

## Dispatchers.IO의 특징 및 스레드 풀 관리

Dispatchers.IO는 I/O 작업(예: 파일 입출력, 네트워크, JDBC 등) 시 스레드가 블록킹되는 것을 고려하여 별도의 스레드 풀에서 실행됩니다. 기본적으로 코루틴 라이브러리 내부에서는 I/O 전용 스레드 풀의 크기를 동적으로 조정하며, 필요에 따라 시스템 속성이나 커스텀 설정을 통해 스레드 풀의 최대 스레드 수 등을 조정할 수 있습니다. 또한, Dispatchers.Default가 포크조인 풀을 사용하여 작업을 작은 단위로 나누어 여러 스레드에서 병렬 처리하는 반면, I/O 작업에서는 블록킹 가능성이 크기 때문에 이를 별도로 관리해야 합니다.

## Dispatchers.IO 사용 시나리오

Dispatchers.IO를 도입해야 하는 대표적인 경우는 **스레드가 블록킹될 때**입니다. 예를 들어, JDBC 드라이버는 네트워크 I/O나 DB 쿼리 실행 중 스레드를 블록킹할 수 있으며, 파일을 읽거나 쓰는 작업 역시 블록킹 될 수 있고, 동기 방식의 네트워크 호출 역시 스레드를 블록킹하는 원인이 됩니다. 이와 같이 블록킹 작업이 발생하면, 동일 스레드에서 순차적으로 실행될 경우 전체 애플리케이션의 성능 저하로 이어질 수 있으므로, Dispatchers.IO를 통해 별도의 스레드 풀에서 작업을 처리하여 동시 실행(parallel execution)을 보장하는 것이 중요합니다.

## 코드 예제 분석

### 기본 async() 사용 시 동작

다음 예제에서는 async()를 별도의 디스패처 없이 호출할 때의 동작을 보여줍니다.

```kotlin
@Test
fun `동시성 테스트`() {
    runBlocking {
        println("Main 시작 - 실행 스레드: ${Thread.currentThread().name}")

        // async()는 runBlocking의 컨텍스트를 상속받으므로, 동일한 스레드에서 실행됩니다.
        val dispatchers = "Default"
        val deferredDefault1 = async {
            contentQuery("$dispatchers-1")
        }
        val deferredDefault2 = async {
            contentQuery("$dispatchers-2")
        }

        // 결과 대기
        val resultDefault = deferredDefault1.await()
        println("[$dispatchers-1] 결과: $resultDefault - 호출 스레드: ${Thread.currentThread().name}")
        val resultDefault2 = deferredDefault2.await()
        println("[$dispatchers-2] 결과: $resultDefault2 - 호출 스레드: ${Thread.currentThread().name}")

        println("Main 종료 - 실행 스레드: ${Thread.currentThread().name}")
    }
}

fun contentQuery(contentAggregation: String): String {
    println("[$contentAggregation] 시작 - 실행 스레드: ${Thread.currentThread().name}")
    // 블로킹 작업을 모방 (예: 2초 대기)
    Thread.sleep(2000)
    println("[$contentAggregation] 완료 - 실행 스레드: ${Thread.currentThread().name}")
    return "Result from $contentAggregation"
}
```

이 예제에서는 runBlocking 내부의 컨텍스트를 상속받은 async()를 사용하므로, 모든 코루틴이 동일한 "Test worker" 스레드에서 실행됩니다. 실제 로그는 다음과 같이 출력됩니다.

```
[Default-1] 시작 - 실행 스레드: Test worker @coroutine#2  
[Default-1] 완료 - 실행 스레드: Test worker @coroutine#2  
[Default-2] 시작 - 실행 스레드: Test worker @coroutine#3  
[Default-2] 완료 - 실행 스레드: Test worker @coroutine#3  
```

이 로그는 [Default-1]과 [Default-2] 작업이 각각 순차적으로 실행되었음을 보여줍니다. 첫 번째 코루틴([Default-1])은 "Test worker @coroutine#2"에서 시작되어 같은 스레드에서 완료되고, 두 번째 코루틴([Default-2])은 "Test worker @coroutine#3"에서 실행됩니다. 즉, 블록킹 작업으로 인해 한 코루틴이 실행되는 동안 다른 코루틴은 대기하게 되어 전체 동시성이 보장되지 않습니다.

### async(Dispatchers.IO) 사용 시 동작

동일한 예제에서 async() 호출 시 Dispatchers.IO를 지정하면, I/O 전용 스레드 풀에서 각 코루틴이 서로 다른 스레드에서 실행됩니다. 예를 들어, 아래와 같이 작성할 수 있습니다.

```kotlin
val deferredIO1 = async(Dispatchers.IO) {
    contentQuery("IO-1")
}

val deferredIO2 = async(Dispatchers.IO) {
    contentQuery("IO-2")
}
```

이 방식에서 실제 로그는 다음과 같이 출력됩니다.

```
[IO-2] 시작 - 실행 스레드: DefaultDispatcher-worker-3 @coroutine#3  
[IO-1] 시작 - 실행 스레드: DefaultDispatcher-worker-1 @coroutine#2  
[IO-2] 완료 - 실행 스레드: DefaultDispatcher-worker-3 @coroutine#3  
[IO-1] 완료 - 실행 스레드: DefaultDispatcher-worker-1 @coroutine#2  
```

이 로그를 통해 각 코루틴이 별도의 스레드(예를 들어 "DefaultDispatcher-worker-1"과 "DefaultDispatcher-worker-3")에서 병렬로 실행됨을 확인할 수 있습니다. 한 코루틴에서 블록킹 작업이 발생하더라도, 다른 코루틴은 별도의 스레드에서 독립적으로 실행되므로 전체 실행 시간이 단축되고 시스템의 반응성이 개선됩니다.

## 실제 사례: JDBC 드라이버와 블록킹 문제

다음은 JDBC 드라이버를 사용하는 페이징 쿼리 예제입니다. 아래 코드에서는 content와 totalCount 두 쿼리를 동시에 실행하도록 작성되었지만, async()에 별도의 디스패처가 지정되지 않아 모든 코루틴이 runBlocking의 컨텍스트를 상속받아 동일한 "Test worker" 스레드에서 순차적으로 실행됩니다.

```kotlin
override fun findPagingBy(pageable: Pageable, address: String): Page<Order> = runBlocking {
    log.info("findPagingBy thread : ${Thread.currentThread()}")
    val content: Deferred<List<Order>> = async() {
        log.info("content thread : ${Thread.currentThread()}")
        from(order)
            .select(order)
            .innerJoin(user).on(order.userId.eq(user.id))
            .leftJoin(coupon).on(order.couponId.eq(coupon.id))
            .where(order.address.eq(address))
            .run {
                querydsl.applyPagination(pageable, this).fetch()
            }
    }
    val totalCount: Deferred<Long> = async() {
        log.info("count thread : ${Thread.currentThread()}")
        from(order)
            .select(order.count())
            .where(order.address.eq(address))
            .fetchFirst()
    }
    PageImpl(content.await(), pageable, totalCount.await())
}
```

아래 이미지는 위 코드가 실행되었을 때의 로그를 보여줍니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/002.png)  
![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/003.png)

이와 같이 async()에 별도의 디스패처를 지정하지 않으면, 한 쿼리의 블록킹 작업이 완료될 때까지 스레드가 점유되어 전체 작업이 순차적으로 실행됩니다. 이 경우, [Default-1]과 [Default-2] 로그에서 볼 수 있듯이, 한 작업이 완료된 후 다음 작업이 시작되므로, JDBC와 같이 블록킹이 발생하는 작업에서는 전체 성능 저하가 발생할 수 있습니다.

반면, 아래와 같이 Dispatchers.IO를 적용하면 두 쿼리가 I/O 전용 스레드 풀에서 병렬로 실행됩니다.

```kotlin
override fun findPagingBy(pageable: Pageable, address: String): Page<Order> = runBlocking {
    log.info("findPagingBy thread : ${Thread.currentThread()}")
    val content: Deferred<List<Order>> = async(Dispatchers.IO) {
        log.info("content thread : ${Thread.currentThread()}")
        from(order)
            .select(order)
            .innerJoin(user).on(order.userId.eq(user.id))
            .leftJoin(coupon).on(order.couponId.eq(coupon.id))
            .where(order.address.eq(address))
            .run {
                querydsl.applyPagination(pageable, this).fetch()
            }
    }
    val totalCount: Deferred<Long> = async(Dispatchers.IO) {
        log.info("count thread : ${Thread.currentThread()}")
        from(order)
            .select(order.count())
            .where(order.address.eq(address))
            .fetchFirst()
    }
    PageImpl(content.await(), pageable, totalCount.await())
}
```

이 경우 실제 실행 로그에서는 각 쿼리가 서로 다른 스레드(예: "DefaultDispatcher-worker-1"과 "DefaultDispatcher-worker-3")에서 실행되어, 한 쿼리의 블록킹이 다른 쿼리의 실행에 영향을 주지 않음을 보여줍니다. 이를 통해 전체 시스템의 성능과 반응성이 크게 개선됨을 확인할 수 있습니다.

## 결론

Kotlin 코루틴의 Dispatchers.IO는 I/O 작업에서 발생하는 스레드 블록킹 문제를 효과적으로 해결할 수 있는 강력한 도구입니다. 각 디스패처는 용도와 실행 방식에서 차이가 있으며, 특히 블록킹 작업이 빈번한 경우에는 Dispatchers.IO를 활용하는 것이 바람직합니다. Dispatchers.IO는 동적 스레드 풀을 사용하여 블록킹 작업이 발생하더라도 다른 코루틴의 실행에 영향을 주지 않고, JDBC와 같이 스레드 블록킹이 발생하는 작업에서도 각 쿼리를 별도의 스레드에서 병렬로 처리할 수 있게 해줍니다. 이와 같이 Dispatchers.IO를 적절히 활용하면, 비동기 I/O 작업에서 발생할 수 있는 문제들을 극복하고 보다 효율적이며 반응성이 뛰어난 애플리케이션을 구현할 수 있을 것입니다.