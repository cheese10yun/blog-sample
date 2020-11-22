# Rx Kotlin 이용해서 성능 개선

Rx Kotlin를 사용하면 스레드를 더 쉽게사용할 수 있있습니다. 

## 시나리오

가장 흔한 플로우로 외부 API를 호출하고 그 결과에 맞게 데이터베이스를 수정하는 방식입니다.

1. 주문을 시스템 내부 API를 호출해서 진행한다.
2. 내부 API 시스템 성공 여부에 따라 Status 를 지정한다.

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
HTTP 통신을 하는 Client 코드입니다. 성공과 실패를 리턴하는 간단한 코드입니다.

## Test 
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
    println(stopWatch.totalTimeSeconds) // 1m 7s
}
```
단일 스레드에서 1,000의 api를 호출 하여 그 결과에 따라서 데이터베이스 상태를업데이트 했을 경우 `1m 44s` 정도 시간이 발생합니다.

##  