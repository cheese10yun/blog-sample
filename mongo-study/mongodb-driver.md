아래는 MongoDB 동기(블로킹) 드라이버가 코루틴 내에서 어떻게 동작하는지, 기본 async()와 async(Dispatchers.IO) 사용 시의 차이점 및 해결 방법에 대해 정리한 내용입니다. 마지막에는 동시성 테스트를 위한 예제 코드까지 추가되어 있어, 실제 로그를 통해 실행 스레드와 병렬 실행 여부를 확인할 수 있습니다.

---

## 목차

1. 동기 MongoDB 드라이버의 블로킹 특성
2. 코루틴과 디스패처의 역할
3. 기본 async()와 Dispatchers.IO 사용 비교
4. 예제 코드: 동시성 테스트
5. 결론 및 요약

---

## 1. 동기 MongoDB 드라이버의 블로킹 특성

- **동기 드라이버**  
  `mongodb-driver-sync`와 같은 동기 방식의 MongoDB 드라이버는 쿼리 실행 시 네트워크 요청과 응답을 처리하는 동안 호출한 스레드를 **완전히 점유(blocking)**합니다.
  - 쿼리를 보내고 결과가 반환되기 전까지 해당 스레드는 다른 작업을 수행할 수 없으므로, 블로킹 I/O 특성이 나타납니다.

- **문제점**
  - 하나의 스레드에서 블로킹 작업이 실행되면, 전체 작업이 순차적으로 처리되어 응답성이 저하될 수 있습니다.

---

## 2. 코루틴과 디스패처의 역할

- **코루틴의 동시성**
  - 코루틴은 논리적으로 비동기 실행을 지원하지만, 실제 작업은 지정된 디스패처의 스레드 풀 내에서 이루어집니다.

- **컨텍스트 상속 vs. 지정된 디스패처**
  - 기본 async()는 부모 코루틴(예, runBlocking)의 컨텍스트를 그대로 상속받아 실행됩니다.
  - 반면, async(Dispatchers.IO)는 I/O 작업에 최적화된 별도의 스레드 풀에서 실행되므로, 블로킹 작업이라 하더라도 서로 다른 스레드에서 실행될 수 있습니다.

---

## 3. 기본 async()와 Dispatchers.IO 사용 비교

- **기본 async() 사용 시**
  ```kotlin
  // 기본 async() 사용: 같은 스레드(main)에서 실행됨
  val contentDefault = async { contentQuery(Query().with(pageable)) }
  val totalCountDefault = async { countQuery(Query()) }
  val pageDefault = PageImpl(contentDefault.await(), pageable, totalCountDefault.await())
  ```
  - runBlocking의 컨텍스트(예: 메인 스레드)를 상속받기 때문에, 두 코루틴이 같은 스레드에서 실행됩니다.
  - MongoDB 동기 드라이버의 블로킹 I/O 특성으로 인해 한 쿼리가 실행되는 동안 다른 쿼리는 대기하게 되어, 순차적으로 실행됩니다.

- **async(Dispatchers.IO) 사용 시**
  ```kotlin
  // async(Dispatchers.IO) 사용: I/O 전용 스레드 풀에서 실행되어 병렬 처리됨
  val content = async(Dispatchers.IO) { contentQuery(Query().with(pageable)) }
  val totalCount = async(Dispatchers.IO) { countQuery(Query()) }
  val pageIO = PageImpl(content.await(), pageable, totalCount.await())
  ```
  - Dispatchers.IO를 사용하면 각 코루틴이 별도의 I/O 스레드에서 실행되어, 하나의 코루틴이 블로킹되어도 다른 코루틴은 다른 스레드에서 동시에 실행됩니다.
  - 이 경우 로그에서 두 쿼리의 실행 시각이 거의 동시에 찍히는 것을 확인할 수 있으며, 물리적 병렬 실행이 이루어집니다.

- **핵심 포인트**
  - MongoDB 드라이버 자체는 동기적이므로, async(Dispatchers.IO)를 사용해도 내부의 블로킹 I/O 동작은 변하지 않습니다.
  - 단지, 블로킹 작업이 별도의 스레드에서 실행되어 전체 애플리케이션의 응답성과 병렬성이 개선되는 효과를 볼 수 있습니다.

---

## 4. 예제 코드: 동시성 테스트

다음 코드는 코루틴에서 기본 async()와 async(Dispatchers.IO)를 사용하여 동시성(병렬 실행) 여부를 확인하는 예제입니다.

```kotlin
import kotlinx.coroutines.*

@Test
fun `동시성 테스트`() {
  runBlocking {
    println("Main 시작 - 실행 스레드: ${Thread.currentThread().name}")

    // async() 기본 컨텍스트 사용: runBlocking의 컨텍스트를 상속받으므로 같은 스레드(main)에서 실행됩니다.
    val deferredDefault = async {
      contentQuery("Default")
    }

    // async(Dispatchers.IO) 사용: I/O 전용 스레드 풀에서 실행됩니다.
    val deferredIO = async(Dispatchers.IO) {
      contentQuery("IO")
    }

    // 결과 대기
    val resultDefault = deferredDefault.await()
    println("[Default] 결과: $resultDefault - 호출 스레드: ${Thread.currentThread().name}")

    val resultIO = deferredIO.await()
    println("[IO] 결과: $resultIO - 호출 스레드: ${Thread.currentThread().name}")

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

### 코드 설명

- **기본 async() 사용**
  - `deferredDefault`는 runBlocking의 기본 컨텍스트를 상속받아 동일한 스레드(예: "Test worker @coroutine#1" 또는 "Test worker @coroutine#2")에서 실행됩니다.
  - 결과적으로, `contentQuery("Default")` 호출 시 메인 스레드에서 2초 동안 블로킹되어 순차적으로 실행되므로, 한 작업이 끝난 후에 다음 작업이 실행됩니다.

- **async(Dispatchers.IO) 사용**
  - `deferredIO`는 Dispatchers.IO를 사용하여 I/O 전용 스레드 풀에서 실행됩니다.
  - `contentQuery("IO")`는 별도의 스레드(예: "DefaultDispatcher-worker-3")에서 실행되므로, 메인 스레드와는 다른 스레드에서 블로킹 작업이 수행되고, 두 작업이 동시에(병렬로) 실행됩니다.

### 실행 결과 로그

**기본 async() 사용 시 로그 (순차 실행):**

```
Main 시작 - 실행 스레드: Test worker @coroutine#1
[Default] 시작 - 실행 스레드: Test worker @coroutine#2
[Default] 완료 - 실행 스레드: Test worker @coroutine#2
[IO] 시작 - 실행 스레드: Test worker @coroutine#3
[IO] 완료 - 실행 스레드: Test worker @coroutine#3
[Default] 결과: Result from Default - 호출 스레드: Test worker @coroutine#1
[IO] 결과: Result from IO - 호출 스레드: Test worker @coroutine#1
Main 종료 - 실행 스레드: Test worker @coroutine#1
```

**async(Dispatchers.IO) 사용 시 로그 (병렬 실행):**

```
Main 시작 - 실행 스레드: Test worker @coroutine#1
[IO] 시작 - 실행 스레드: DefaultDispatcher-worker-3 @coroutine#3
[Default] 시작 - 실행 스레드: DefaultDispatcher-worker-1 @coroutine#2
[Default] 완료 - 실행 스레드: DefaultDispatcher-worker-1 @coroutine#2
[IO] 완료 - 실행 스레드: DefaultDispatcher-worker-3 @coroutine#3
[Default] 결과: Result from Default - 호출 스레드: Test worker @coroutine#1
[IO] 결과: Result from IO - 호출 스레드: Test worker @coroutine#1
Main 종료 - 실행 스레드: Test worker @coroutine#1
```

### 요약

- **기본 async() 사용:**  
  모든 작업이 동일한 runBlocking 컨텍스트(메인 스레드)에서 순차적으로 실행되어, 한 작업이 블로킹되면 다음 작업이 대기하게 됩니다.

- **async(Dispatchers.IO) 사용:**  
  각 작업이 I/O 전용 스레드 풀의 별도 스레드에서 실행되어, 동시에 블로킹 작업이 이루어지는 효과를 확인할 수 있습니다.

이 예제를 통해 MongoDB 동기 드라이버가 블로킹 I/O를 수행할 때, 기본 컨텍스트에서는 동시성이 발휘되지 않지만, Dispatchers.IO를 사용하면 실제 병렬 실행 효과를 얻을 수 있음을 로그를 통해 확인할 수 있습니다.

## 5. 결론 및 요약

- **동기 MongoDB 드라이버의 특성**
  - 동기적 MongoDB 드라이버는 블로킹 I/O를 수행하므로, 단일 스레드에서 실행될 경우 쿼리 작업이 순차적으로 처리됩니다.

- **코루틴 디스패처 활용**
  - 기본 async()는 부모 코루틴의 컨텍스트(주로 메인 스레드)를 상속받아 실행되므로, 블로킹 I/O 작업이 순차적으로 이루어집니다.
  - async(Dispatchers.IO)를 사용하면, I/O 전용 스레드 풀 내의 서로 다른 스레드에서 블로킹 작업을 실행하므로, 병렬 실행 효과를 얻을 수 있습니다.

- **실제 적용 시 주의사항**
  - Dispatchers.IO를 사용해도 MongoDB 드라이버의 블로킹 특성은 변하지 않으므로, 데이터베이스 커넥션 풀 설정 등도 함께 고려해야 합니다.
  - 충분한 스레드와 커넥션 풀 크기를 확보해야, 병렬 실행 효과를 온전히 발휘할 수 있습니다.

이와 같이, 동기적 MongoDB 드라이버 환경에서도 코루틴의 디스패처를 적절히 활용하면, 블로킹 I/O 작업을 별도의 스레드에서 실행시켜 실제 동시성을 확보하고, 전체 처리 시간을 단축하는 효과를 확인할 수 있습니다.