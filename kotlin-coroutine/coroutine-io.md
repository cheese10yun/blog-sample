# 코루틴 Dispatchers.IO로 블록킹 문제 해결하기

## 서론

Kotlin 코루틴은 복잡한 스레드 관리와 동시성 문제를 간단하게 해결할 수 있는 강력한 도구입니다. 이 글에서는 특히 I/O 작업에 최적화된 `Dispatchers.IO`에 집중하여, 이 디스패처가 어떻게 스레드 블록킹 문제를 해결하고 전체 애플리케이션의 성능과 반응성을 개선할 수 있는지 구체적인 코드 예제와 실행 로그를 통해 살펴봅니다. 또한, JDBC 드라이버나 파일 입출력, 동기식 네트워크 호출 등 블록킹이 발생할 수 있는 실제 상황에서 `Dispatchers.IO`를 적용하는 방법과 그 효과를 자세히 설명하여, 개발자가 실제 프로젝트에서 효율적인 비동기 처리를 구현할 수 있도록 돕는 것을 주요 목적으로 합니다.

## Dispatchers 종류 비교

아래 테이블은 각 디스패처의 특징과 사용 용도를 간략하게 정리한 것입니다.

| **디스패처**               | **설명**                                            | **사용 시나리오**                        | **실행 스레드 예시**                                |
|------------------------|---------------------------------------------------|------------------------------------|----------------------------------------------|
| Dispatchers.Main       | UI 스레드에서 실행되며, 메인 스레드 업데이트를 담당합니다.                | UI 업데이트, 사용자 이벤트 처리                | Android의 메인 스레드 등                            |
| Dispatchers.Default    | CPU 집약적인 작업에 적합하며, 공용 스레드 풀을 사용합니다.               | 복잡한 계산, 데이터 처리                     | 일반적인 백그라운드 작업 스레드                            |
| Dispatchers.Unconfined | 호출한 컨텍스트에 묶이지 않고, 일시적으로 다른 스레드에서 실행될 수 있습니다.      | 빠른 초기 작업, 스레드 전환 없이 동작 확인 시        | 컨텍스트에 따라 달라짐                                 |
| Dispatchers.IO         | I/O 작업에 최적화된 별도의 스레드 풀을 사용하며, 블록킹 작업에 대응할 수 있습니다. | 파일 I/O, 네트워크, DB 접근 등 블록킹이 발생하는 작업 | "DefaultDispatcher-worker-#" 와 같은 I/O 전용 스레드 |

`Dispatchers.IO`는 특히 I/O 작업에서 스레드가 블록킹(blocking) 되는 상황에 적합하여, 별도의 스레드 풀을 사용함으로써 전체 애플리케이션의 응답성을 유지하는 데 큰 역할을 합니다.  
이번 포스팅에서는 특히 `Dispatchers.IO`에 대해 자세히 알아보고, 이를 통해 효율적인 비동기 처리와 블록킹 문제 해결 방안을 알아보겠습니다.

## Dispatchers.IO 사용 시나리오

`Dispatchers.IO`를 도입해야 하는 대표적인 경우는 **스레드가 블록킹될 때**입니다. 예를 들어

- **JDBC 드라이버:** JDBC 드라이버는 네트워크 I/O나 DB 쿼리 실행 중 스레드를 블록킹합니다.
- **파일 입출력:** 파일을 읽거나 쓰는 작업 역시 블록킹 될 수 있습니다.
- **네트워크 요청:** 동기 방식의 네트워크 호출 역시 블록킹을 유발할 수 있습니다.

이와 같이 블록킹 작업이 발생하면, 동일 스레드에서 순차적으로 실행될 경우 전체 애플리케이션의 성능 저하로 이어질 수 있으므로, **`Dispatchers.IO`를 통해 별도의 스레드 풀에서 작업을 처리하여 동시 실행(parallel execution)을 보장하는 것이 중요합니다.**

아래는 async()를 호출할 때 별도의 디스패처를 지정하지 않은 경우의 코드 예제와 그에 따른 실행 로그를 바탕으로 병렬 실행이 어떻게 이루어지는지 구체적으로 설명한 내용입니다.

## Dispatchers.IO 코드 예제 분석

```kotlin
@Test
fun `동시성 테스트`() {
    runBlocking {
        println("Main 시작 - 실행 스레드: ${Thread.currentThread().name}")
        val stopWatch = StopWatch()
        stopWatch.start()

        val deferred1 = async { doSomething("deferred1") }
        val deferred2 = async { doSomething("deferred2") }

        // 결과 대기
        val resultDefault = deferred1.await()
        println("deferred1 결과: $resultDefault - 호출 스레드: ${Thread.currentThread().name}")

        val resultIO = deferred2.await()
        println("deferred2 결과: $resultIO - 호출 스레드: ${Thread.currentThread().name}")

        stopWatch.stop()
        println("소요 시간 : ${stopWatch.totalTimeMillis} ms")
        println("Main 종료 - 실행 스레드: ${Thread.currentThread().name}")
    }
}

private fun doSomething(dispatchersName: String): String {
    println("[$dispatchersName] 시작 - 실행 스레드: ${Thread.currentThread().name}")
    // 2,000 ms 대기
    runBlocking { delay(2000) }
    println("[$dispatchersName] 완료 - 실행 스레드: ${Thread.currentThread().name}")
    return "Result from $dispatchersName"
}
```

`async()`를 호출할 때 별도의 디스패처를 지정하지 않으면, 해당 코루틴은 상위 코루틴의 컨텍스트(여기서는 runBlocking의 컨텍스트)를 그대로 상속받게 됩니다. 이 예제에서는 `doSomething()` 함수가 두 번 호출되며, 각각 2,000ms의 지연(delay)을 포함한 블록킹 작업을 수행한다고 가정합니다. 이 경우, 두 작업은 `async`를 통해 병렬로 실행되므로 이론상 전체 소요 시간은 2,000ms 내외여야 합니다.

실제 실행 로그는 다음과 같습니다.

```
Main 시작 - 실행 스레드: Test worker @coroutine#1
[deferred1] 시작 - 실행 스레드: Test worker @coroutine#2
[deferred2] 시작 - 실행 스레드: Test worker @coroutine#3
[deferred2] 완료 - 실행 스레드: Test worker @coroutine#3
[deferred1] 완료 - 실행 스레드: Test worker @coroutine#2
deferred1 결과: Result from deferred1 - 호출 스레드: Test worker @coroutine#1
deferred2 결과: Result from deferred2 - 호출 스레드: Test worker @coroutine#1
소요 시간 : 2020 ms
Main 종료 - 실행 스레드: Test worker @coroutine#1
```

로그를 분석해보면, Main 코루틴은 "Test worker @coroutine#1" 스레드에서 시작되고, async로 생성된 두 자식 코루틴은 각각 "Test worker @coroutine#2"와 "Test worker @coroutine#3" 스레드에서 실행됩니다. 두 코루틴은 독립적으로 동시에 실행되기 때문에, `doSomething()` 함수 내에서 2,000ms의 대기가 발생하더라도 두 작업이 병렬로 처리되어 전체 소요 시간은 약 2,000ms(실제 2020ms)로 측정됩니다.

**즉, `async()`를 통해 생성된 두 코루틴이 부모의 컨텍스트를 상속받더라도, 각각의 코루틴이 별도의 스레드에서 실행되어 병렬 처리가 이루어지는 것을 확인할 수 있습니다.** 이는 동일한 스레드에서 순차적으로 처리될 경우(예: 동기 호출 시 4,000ms 소요)와 비교했을 때, 전체 실행 시간을 크게 단축시키는 효과가 있음을 보여줍니다.

### 기본 async() 사용 시 동작 (Thread.sleep 사용)

아래 코드는 `doSomething()` 함수 내부에서 `delay(2000)` 대신 `Thread.sleep(2000)`을 사용한 경우입니다.

```kotlin
private fun doSomething(dispatchersName: String): String {
    println("[$dispatchersName] 시작 - 실행 스레드: ${Thread.currentThread().name}")
    // 2,000 ms 대기 (Thread.sleep 사용)
    Thread.sleep(2000)
    println("[$dispatchersName] 완료 - 실행 스레드: ${Thread.currentThread().name}")
    return "Result from $dispatchersName"
}
```

이 경우 실행 로그는 다음과 같이 나타납니다.

```
Main 시작 - 실행 스레드: Test worker @coroutine#1
[deferred1] 시작 - 실행 스레드: Test worker @coroutine#2
[deferred1] 완료 - 실행 스레드: Test worker @coroutine#2
[deferred2] 시작 - 실행 스레드: Test worker @coroutine#3
[deferred2] 완료 - 실행 스레드: Test worker @coroutine#3
deferred1 결과: Result from deferred1 - 호출 스레드: Test worker @coroutine#1
deferred2 결과: Result from deferred2 - 호출 스레드: Test worker @coroutine#1
소요 시간 : 4021 ms
Main 종료 - 실행 스레드: Test worker @coroutine#1
```

기본 async() 사용 시 동작(`Thread.sleep` 사용)에서는, 별도의 디스패처를 지정하지 않아 상위 코루틴의 컨텍스트를 그대로 상속받게 됩니다. 이 경우, runBlocking 내부의 메인 스레드인 Test worker가 블로킹되기 때문에, `doSomething()` 함수 내의 `Thread.sleep(2000)`이 호출되면 해당 스레드가 2,000ms 동안 점유되고, 첫 번째 코루틴(deferred1)이 완료되어야만 두 번째 코루틴(deferred2)이 실행될 수 있습니다. 그 결과, 두 작업이 실제로 순차적으로 실행되어 전체 소요 시간이 약 4021ms로 측정되는 것입니다.

### Dispatchers.IO를 적용한 경우

이 문제를 해결하기 위해 async() 호출 시 `Dispatchers.IO`를 지정하면, 각 코루틴이 I/O 전용 스레드 풀에서 실행되므로 별도의 스레드에서 동시에 작업이 수행됩니다. 수정된 코드는 다음과 같습니다.

```kotlin
val deferred1 = async(Dispatchers.IO) { doSomething("deferred1") }
val deferred2 = async(Dispatchers.IO) { doSomething("deferred2") }
```

이때 실행 로그는 아래와 같이 나타납니다.

```
Main 시작 - 실행 스레드: Test worker @coroutine#1
[deferred2] 시작 - 실행 스레드: DefaultDispatcher-worker-3 @coroutine#3
[deferred1] 시작 - 실행 스레드: DefaultDispatcher-worker-1 @coroutine#2
[deferred2] 완료 - 실행 스레드: DefaultDispatcher-worker-3 @coroutine#3
[deferred1] 완료 - 실행 스레드: DefaultDispatcher-worker-1 @coroutine#2
deferred1 결과: Result from deferred1 - 호출 스레드: Test worker @coroutine#1
deferred2 결과: Result from deferred2 - 호출 스레드: Test worker @coroutine#1
소요 시간 : 2018 ms
Main 종료 - 실행 스레드: Test worker @coroutine#1
```

여기서 확인할 수 있듯이, Main 코루틴은 Test worker에서 시작하지만, deferred1과 deferred2 코루틴은 각각 DefaultDispatcher-worker-1과 DefaultDispatcher-worker-3과 같이 별도의 스레드에서 실행됩니다. 각 코루틴이 독립된 스레드에서 실행되기 때문에, `doSomething()` 함수 내부의 `Thread.sleep(2000)`과 같은 블록킹 호출이 해당 코루틴의 스레드에만 영향을 미치며, 다른 코루틴의 실행에는 영향을 주지 않습니다. 그 결과, 두 작업이 동시에 병렬로 실행되어 전체 소요 시간이 약 2018ms로 단축되는 효과를 얻을 수 있습니다.

```kotlin
private fun doSomething(dispatchersName: String): String {
    // ...
    // runBlocking { delay(2000) } // delay 에서 Thread sleep 으로 대체
    Thread.sleep(2000)
    // ...
    return "Result from $dispatchersName"
}
```

기존 delay에서 `Thread.sleep(2000)` 으로 변경 했을 경우에 로그를 보자

```
Main 시작 - 실행 스레드: Test worker @coroutine#1
[deferred1] 시작 - 실행 스레드: Test worker @coroutine#2
[deferred1] 완료 - 실행 스레드: Test worker @coroutine#2
[deferred2] 시작 - 실행 스레드: Test worker @coroutine#3
[deferred2] 완료 - 실행 스레드: Test worker @coroutine#3
deferred1 결과: Result from deferred1 - 호출 스레드: Test worker @coroutine#1
deferred2 결과: Result from deferred1 - 호출 스레드: Test worker @coroutine#1
소요 시간 : 4021 ms
Main 종료 - 실행 스레드: Test worker @coroutine#1
```

`doSomething()` 함수가 @coroutine#2와 @coroutine#3에서 각각 실행되었음에도 불구하고, 전체 소요 시간이 4021ms로 측정된다는 것은 `async()`를 사용할 때 의도한 병렬 실행이 이루어지지 않았음을 의미합니다. 그 이유는, `async()`에 별도의 디스패처를 지정하지 않아 상위 코루틴(runBlocking)의 컨텍스트를 그대로 상속받게 되면서, Main 스레드인 Test worker가 deferred1의 실행으로 인해 블록킹되고, deferred2도 동일한 Test worker에서 실행되기 때문입니다. 즉, Test worker 스레드가 블록킹이 해제될 때까지 전체 작업이 순차적으로 실행됩니다.

이 문제를 해결하기 위해, 각 `async()` 호출 시 `Dispatchers.IO`와 같이 별도의 I/O 전용 스레드 풀을 지정하면, 각 코루틴이 상위 컨텍스트를 상속받지 않고 독립적인 스레드에서 실행되게 됩니다. 다음과 같이 코드를 수정하면, deferred1과 deferred2가 각각 다른 스레드에서 동시에 블록킹 작업을 수행하므로, 전체 소요 시간이 약 2000ms 내외로 단축됩니다.

```kotlin
val deferred1 = async(Dispatchers.IO) { doSomething("deferred1") }
val deferred2 = async(Dispatchers.IO) { doSomething("deferred2") }
```

이렇게 수정한 후 로그를 확인하면, Main 코루틴은 여전히 Test worker에서 실행되지만, deferred1은 DefaultDispatcher-worker-1, deferred2는 DefaultDispatcher-worker-3과 같이 서로 다른 스레드에서 실행되어 병렬 처리가 이루어지는 것을 확인할 수 있습니다.

```
Main 시작 - 실행 스레드: Test worker @coroutine#1
[deferred2] 시작 - 실행 스레드: DefaultDispatcher-worker-3 @coroutine#3
[deferred1] 시작 - 실행 스레드: DefaultDispatcher-worker-1 @coroutine#2
[deferred2] 완료 - 실행 스레드: DefaultDispatcher-worker-3 @coroutine#3
[deferred1] 완료 - 실행 스레드: DefaultDispatcher-worker-1 @coroutine#2
deferred1 결과: Result from deferred1 - 호출 스레드: Test worker @coroutine#1
deferred2 결과: Result from deferred1 - 호출 스레드: Test worker @coroutine#1
소요 시간 : 2018 ms
Main 종료 - 실행 스레드: Test worker @coroutine#1
```

Test worker 으로 메인 스레드가 시작하는 것을 확인할 수 있고 deferred1, deferred2의 수행 스레드가 DefaultDispatcher-worker-1, DefaultDispatcher-worker-3 으로 각기 다른 스레드를 통해서 수행되는 것을 확인할 수 있습니다.

각기 다른 스레드로 동작하기 때문에 각각의 스레드가 블록킹 당해도 동시에 수행이 가능하며 소요 시간이 2018 ms 으로 동작하는 것을 확인할 수 있습니다. 이 처럼 스레드가 블록킹 당하는 경우라면 `Dispatchers.IO` 가 적절한 대안이 될 수 있습니다.

## 실제 사례: JDBC 드라이버와 블록킹 문제

다음은 JDBC 드라이버를 사용하는 페이징 쿼리 예제입니다. 아래 코드에서는 content와 totalCount 두 쿼리를 동시에 실행하도록 작성되었지만, `async()`에 별도의 디스패처를 지정하지 않아 모든 코루틴이 runBlocking의 컨텍스트를 상속받아 동일한 스레드에서 순차적으로 실행됩니다.

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

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/refs/heads/master/kotlin-coroutine/images/00001.png)

여기서 특히 강조해야 할 점은, **JDBC 드라이버가 기본적으로 블록킹 I/O를 수행한다는 것입니다.** JDBC 드라이버는 데이터베이스와의 통신 과정에서 네트워크 I/O 및 쿼리 실행을 진행하는 동안 스레드를 블록킹합니다. 이로 인해 동일한 스레드에서 쿼리가 순차적으로 실행될 경우, 한 쿼리의 블록킹이 다른 쿼리의 실행까지 지연시키게 됩니다. 위 예제에서는 `async()`에 별도의 디스패처를 지정하지 않아, content와 totalCount 쿼리가 모두 runBlocking의 컨텍스트인 **동일한 스레드에서 실행됩니다. 이로 인해 한 쿼리의 작업이 완료되어야만 다음 쿼리가 실행되므로 전체 성능 저하와 응답성 저하가 발생할 수 있습니다.**

이를 해결하기 위해, 아래와 같이 `async()` 호출 시 `Dispatchers.IO`와 같은 I/O 전용 스레드 풀을 지정하면, 각 코루틴이 독립된 별도의 스레드에서 실행됩니다.

```kotlin
val content: Deferred<List<Order>> = async(Dispatchers.IO) { ... }
val totalCount: Deferred<Long> = async(Dispatchers.IO) { ... }
```

이렇게 하면 JDBC 드라이버의 블록킹 특성에도 불구하고, **각 쿼리가 독립적으로 다른 스레드에서 병렬로 실행되므로, 한 쿼리의 블록킹이 다른 쿼리의 실행에 영향을 주지 않습니다.** 실제로 이 방식으로 실행할 경우, 전체 성능이 크게 향상되어 병렬 처리의 이점을 얻을 수 있습니다.

새롭게 추가된 로그 이미지를 보면, Main 코루틴은 여전히 동일한 스레드에서 실행되지만, content와 totalCount 코루틴은 각각 "DefaultDispatcher-worker-1" 및 "DefaultDispatcher-worker-3"과 같은 별도의 스레드에서 실행되어, 동시에 작업을 수행할 수 있습니다. **이와 같이, 블록킹 I/O가 발생하는 환경에서는 `Dispatchers.IO`를 활용하여 각 작업을 독립적인 스레드에서 처리하는 것이 전체 시스템의 응답성과 성능 개선에 효과적입니다.**

## 결론

이번 포스팅에서는 Kotlin 코루틴의 `Dispatchers.IO`가 어떻게 블록킹 I/O 작업, 특히 JDBC 드라이버나 파일 입출력과 같은 상황에서 전체 애플리케이션의 성능과 반응성을 개선하는 데 기여하는지 살펴보았습니다. 기본적으로 `async()`를 호출할 때 별도의 디스패처를 지정하지 않으면, 상위 코루틴의 컨텍스트를 그대로 상속받아 동일한 스레드에서 순차적으로 실행되기 때문에, 블록킹 작업이 발생할 경우 전체 실행 시간이 크게 늘어나는 문제가 발생합니다. 그러나, `Dispatchers.IO`를 지정하면 각 코루틴이 I/O 전용 스레드 풀의 독립된 스레드에서 실행되어, 한 작업의 블록킹이 다른 작업에 영향을 주지 않고 병렬 처리가 가능해집니다. 이를 통해 실제 작업에서는 전체 처리 시간이 단축되고, 시스템의 응답성이 크게 향상됨을 확인할 수 있습니다.

이와 같이 `Dispatchers.IO`를 적절히 활용하면, 블록킹 I/O로 인한 성능 저하를 효과적으로 극복할 수 있으며, 효율적인 비동기 처리와 병렬 실행을 통해 더욱 안정적이고 반응성이 뛰어난 애플리케이션을 구현할 수 있습니다.