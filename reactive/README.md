# Rx Kotlin 이용해서 성능 개선

Rx Kotlin를 사용하면 스레드를 더 쉽게 사용할 수 있습니다.

## 시나리오

가장 흔한 케이스로 외부 API를 호출하고 그 결과에 맞게 데이터베이스를 수정하는 방식입니다.

1. 주문을 시스템 내부 API를 호출해서 진행한다.
2. 내부 API 시스템 성공 여부에 따라 status를 지정한다.

## Code

### Entity
```kotlin
@Entity
@Table(name = "orders")
class Order(
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    override fun toString(): String {
        return "Order(status=$status, id=$id)"
    }   
}

enum class OrderStatus {
    READY,
    COMPLETED,
    FAILED
}
```
엔티티 코드는 간단합니다. id, status를 가지고 있습니다.

```kotlin
class OrderHttpClient {

    /**
     * 외부 IO 작업을 진행합니다. block은 100 ms, 80% 성공한다
     */
    fun doSomething(orderId: Long): Boolean {
        runBlocking {
            delay(100)
        }
        val random = Random.nextInt(0, 10)
        return 8 > random
    }
}
```
HTTP 통신을 하는 Client 코드입니다. 성공과 실패를 리턴하는 간단한 코드입니다. 해당 코드를 호출하면 `100ms` 블록이 걸리게 설정했습니다.

## Test 

### 단일 스레드
```kotlin
@Test
fun `단일 스레드 작업`() {
    val stopWatch = StopWatch()
    val orders = givenOrders(1_000) // READY Status Order를 데이터베이스에 저장함
    stopWatch.start()

    orders
        .forEach {
            val result = sampleApi.doSomething(it.id!!)
            when {
                result -> it.status = OrderStatus.COMPLETED
                else -> it.status = OrderStatus.FAILED
            }
        }

    stopWatch.stop()
    println(stopWatch.totalTimeSeconds)
}
```
단일 스레드에서 5,000의 api를 호출하여 그 결과에 따라서 데이터베이스 상태를 업데이트하는 코드입니다.

### 멀티 스레드

```kotlin
@Test
fun `멀티 스레드 작업`() {
    val stopWatch = StopWatch()
    val orders = givenOrders(5_000) // (1)
    stopWatch.start()

    orders
        .toFlowable() //(2)
        .parallel() //(3)
        .runOn(Schedulers.io()) //(4)
        .map {
            println("Mapping orderId :${it.id} ${Thread.currentThread().name}")
            val result = sampleApi.doSomething(it.id!!) 
            Pair(result, it)
        }
        .sequential() //(5)
        .subscribe(
            {
                println("Received orderId :${it.second.id} ${Thread.currentThread().name}")
                when {
                    it.first -> it.second.status = OrderStatus.COMPLETED
                    else -> it.second.status = OrderStatus.FAILED
                }
            },
            {
                it.printStackTrace()
            },
            {
                stopWatch.stop()
                println(stopWatch.totalTimeSeconds)
            }
        )
    runBlocking { delay(5_000) } //(6)
}
```
* (1): order 데이터를 준비합니다.
* (2): Back Pressure 기능을 제공하는 Flowable으로 생성 생성
* (3): CPU 수와 동일하게 ParallelFlowable을 생성할 수 있게 해줍니다.
* (4): ParallelFlowable의 병렬 처리 수준만큼 Scheduler.createWorker를 호출해서 스레드를 생성합니다. Buffer size는 기본 설정 128개와 동일합니다.
* (5): 각 ParallelFlowable의 값을 병합 작업을 진행합니다.
* (6): 해당 작업이 모두 테스트 스레드 `Test worker`에서 실행되지 않기 때문에 block을 진행합니다.


#### parallel
![](https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/flowable.parallel.png)

`toFlowable()` 메서드로 Flowable 처리를 진행했던 것을 parallel 처리하기 위해서 `parallel()` 메서드를 사용합니다. 해당 레일은 자체적으로 병렬로 실행되지 않으며 각 레일이 병렬로 실행하려면 `runOn()`메서드의 호출이 필요합니다. 이때 `Schedulers.io()`를 사용합니다. `Schedulers.io()`는 `I/O` 관련 작업을 수행할 수 있는 무제한의 워커 스레드를 생성하는 스레드를 제공한다. 해당 테스트 환경은 12 코어기 때문에 12 스레드를 사용하게 됩니다.

#### sequential
![](https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/parallelflowable.sequential.png)

`parallel`메서 여러 레일을 생성하는 것을 다시 단일 스퀀스로 병합하기 위해서 사용합니다.


```
...
Mapping orderId :15 RxCachedThreadScheduler-3
Mapping orderId :18 RxCachedThreadScheduler-6
Mapping orderId :22 RxCachedThreadScheduler-10
Received orderId :8 RxCachedThreadScheduler-8
Mapping orderId :16 RxCachedThreadScheduler-4
Received orderId :1 RxCachedThreadScheduler-8
Mapping orderId :23 RxCachedThreadScheduler-11
Received orderId :2 RxCachedThreadScheduler-8
Received orderId :3 RxCachedThreadScheduler-8
Received orderId :4 RxCachedThreadScheduler-8
Mapping orderId :14 RxCachedThreadScheduler-2
Received orderId :5 RxCachedThreadScheduler-8
Mapping orderId :24 RxCachedThreadScheduler-12
...
```
`Mapping`, `Received` 스레드를 확인 1~12 스레드를 모두 사용하는 것을 확인할 수 있습니다. 해당 스레드는 모두 메인 스레드인 `Test worker`에서 진행되지 않습니다.

## 비교
데이터 개수 | 스레드 | 소요 시간
-------|-----|------
5,000 | 단일스레드 | 8m 58s
5,000 | 12 스레드 | 43s

실행 환경의 CPU Core 수에 따라서 결과는 많이 달라집니다.

