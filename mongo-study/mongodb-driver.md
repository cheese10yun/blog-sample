아래는 MongoDB의 동기(블로킹) 드라이버가 코루틴 내에서 어떻게 동작하며, 왜 기본 async()와 async(Dispatchers.IO)를 사용했을 때 실행 방식이 달라지는지에 대해 정리한 내용입니다.

---

## 목차

1. 동기(블로킹) MongoDB 드라이버의 동작 방식
2. 코루틴과 디스패처의 역할
3. Dispatchers.IO를 통한 병렬 실행 효과
4. 실제 예제 코드와 로그 분석
5. 주의사항 및 고려사항

---

## 1. 동기(블로킹) MongoDB 드라이버의 동작 방식

- **동기 드라이버 특성**:  
  `mongodb-driver-sync`는 동기 방식의 드라이버로, 쿼리 실행 시 네트워크 I/O를 처리하면서 호출한 스레드를 **완전히 점유(blocking)**합니다.
    - 예를 들어, 쿼리를 보내고 결과가 돌아올 때까지 해당 스레드는 다른 작업을 수행하지 못합니다.

- **실행 예**:
  ```kotlin
  protected fun <S : T> applyPagination(
      pageable: Pageable,
      contentQuery: (Query) -> List<S>,
      countQuery: (Query) -> Long
  ) = runBlocking {
      val content = async() { contentQuery(Query().with(pageable)) }
      val totalCount = async() { countQuery(Query()) }
      PageImpl(content.await(), pageable, totalCount.await())
  }
  ```
    - 위 코드에서 기본 async()를 사용하면, runBlocking의 컨텍스트(보통 메인 스레드)를 상속받아 실행됩니다.
    - 이 경우 두 async 코루틴은 **같은 스레드**에서 순차적으로 실행되므로, 한 쿼리가 끝나야 다음 쿼리가 실행됩니다.
    - 로그에서 두 쿼리 사이에 1초 정도의 차이가 발생한 것을 확인할 수 있습니다.

---

## 2. 코루틴과 디스패처의 역할

- **코루틴의 동시성**:  
  코루틴은 논리적으로는 비동기(동시) 실행이 가능하지만, 실제 실행은 지정한 디스패처의 스레드 풀 내에서 이루어집니다.

- **디스패처 지정**:
    - 기본적으로 `async()`는 부모 코루틴의 컨텍스트(예: runBlocking의 메인 스레드)를 그대로 사용합니다.
    - 반면, `async(Dispatchers.IO)`를 사용하면 I/O 전용 스레드 풀에서 실행되므로, 블로킹 작업이더라도 **다른 스레드**에서 병렬로 실행할 수 있습니다.

---

## 3. Dispatchers.IO를 통한 병렬 실행 효과

- **블로킹 I/O 작업의 병렬 처리**:
    - **기본 async() 사용 시**:  
      두 코루틴 모두 같은 스레드(예: main)에서 실행되므로, 하나의 작업이 블로킹되면 다른 작업도 기다려야 합니다.

    - **async(Dispatchers.IO) 사용 시**:  
      각 코루틴이 별도의 I/O 스레드에서 실행되므로, 하나의 코루틴이 블로킹되어 있더라도 다른 코루틴은 다른 스레드에서 동시에 진행됩니다.

- **결과**:
    - 두 작업이 서로 다른 스레드에서 병렬로 실행되므로, 전체 실행 시간은 개별 작업 중 **가장 긴 작업 시간** 정도로 단축됩니다.

- **실제 로그 비교**:
    - 기본 async() 사용 시:
      ```
      2025-03-05 09:10:06.683 DEBUG ... : find using query: ...  // contentQuery 시작
      2025-03-05 09:10:07.647 DEBUG ... : Executing count: ...      // countQuery 시작 (1초 후)
      ```
    - async(Dispatchers.IO) 사용 시:
      ```
      2025-03-05 09:12:40.572 DEBUG ... : find using query: ...  // contentQuery 시작
      2025-03-05 09:12:40.572 DEBUG ... : Executing count: ...      // countQuery 시작 (동시)
      ```

---

## 4. 실제 예제 코드와 로그 분석

### 예제 코드

```kotlin
protected fun <S : T> applyPagination(
    pageable: Pageable,
    contentQuery: (Query) -> List<S>,
    countQuery: (Query) -> Long
) = runBlocking {
    // 기본 async() 사용: 같은 스레드(main)에서 실행됨
    val contentDefault = async { contentQuery(Query().with(pageable)) }
    val totalCountDefault = async { countQuery(Query()) }
    val pageDefault = PageImpl(contentDefault.await(), pageable, totalCountDefault.await())

    // async(Dispatchers.IO) 사용: I/O 전용 스레드에서 실행되어 병렬 처리됨
    val content = async(Dispatchers.IO) { contentQuery(Query().with(pageable)) }
    val totalCount = async(Dispatchers.IO) { countQuery(Query()) }
    val pageIO = PageImpl(content.await(), pageable, totalCount.await())

    // 두 결과 중 하나를 반환 (예시로 pageIO)
    pageIO
}
```

### 로그 분석

- **기본 async() 사용 시**:
    - 두 코루틴 모두 runBlocking의 컨텍스트(메인 스레드)에서 실행되어, 하나의 쿼리가 완료된 후 다음 쿼리가 실행됩니다.
    - 이로 인해 쿼리 간 실행 간격(예: 1초 차이)이 발생합니다.

- **async(Dispatchers.IO) 사용 시**:
    - 두 코루틴이 각각 I/O 스레드 풀에서 실행되어, 두 쿼리가 **동시에** 시작됩니다.
    - 결과적으로 로그의 타임스탬프가 동일하거나 거의 동시에 찍히게 됩니다.

---

## 5. 주의사항 및 고려사항

1. **스레드 풀 크기**
    - `Dispatchers.IO`는 I/O 작업에 최적화된 별도의 스레드 풀을 사용하지만, 풀의 최대 스레드 수에는 제한이 있습니다.
    - 동시 실행되는 작업 수가 너무 많으면 스레드 부족 문제가 발생할 수 있습니다.

2. **DB 커넥션 풀**
    - MongoDB 드라이버 내부의 커넥션 풀 설정이 낮으면, 여러 스레드에서 병렬로 쿼리를 요청하더라도 결국 단일 커넥션을 공유하게 되어 순차 실행될 수 있습니다.
    - 커넥션 풀의 설정(최소/최대 커넥션 수)을 확인해야 합니다.

3. **블로킹 작업의 본질**
    - Dispatchers.IO를 사용해도 각 작업은 여전히 블로킹 I/O를 수행합니다.
    - 단지, 다른 작업을 위해 별도의 스레드가 할당되어 전체적으로 병렬 실행되는 효과를 볼 수 있다는 점을 기억해야 합니다.

---

## 결론

- **동기 MongoDB 드라이버**는 블로킹 I/O를 수행하므로, 기본 async()를 사용할 경우 runBlocking의 컨텍스트(메인 스레드) 내에서 순차적으로 실행됩니다.
- **코루틴과 Dispatchers.IO**를 활용하면, 각 블로킹 작업이 서로 다른 스레드에서 실행되어, 물리적으로는 병렬 실행되는 효과를 얻을 수 있습니다.
- 따라서, async(Dispatchers.IO)를 사용하면 로그에서 두 쿼리가 동시에 실행되는 것을 확인할 수 있으며, 전체 실행 시간을 단축할 수 있습니다.

이와 같이, 코루틴 내에서 블로킹 I/O 작업을 별도의 I/O 스레드에서 실행시키면, MongoDB 드라이버가 동기적이라 하더라도 물리적으로 병렬 실행되는 효과를 얻을 수 있습니다.