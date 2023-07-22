# Stock


```kotlin

(정리 추가 1-1)
// 통과
@Transactional(isolation = Isolation.READ_UNCOMMITTED) // 통과 O
@Transactional(isolation = Isolation.READ_COMMITTED) // 통과 X
@Transactional(isolation = Isolation.REPEATABLE_READ) // 통과 X
@Transactional(isolation = Isolation.SERIALIZABLE) // 통과 O

@Synchronized
fun decrease(stockId: Long, quantity: Long) {
    val stock = stockRepository.findByIdOrNull(stockId)!!
    stock.decrease(quantity)
    stockRepository.saveAndFlush(stock)
}

```


```kotlin

@Service
class OptimisticLockStockService(
    private val stockRepository: StockRepository,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun decrease(stockId: Long, quantity: Long) {
        val stock = stockRepository.findByIdWithOptimisticLock(stockId)
        stock.decrease(quantity)
        stockRepository.saveAndFlush(stock)
    }
}

@Service
class OptimisticLockStockFaceService(
    private val optimisticLockStockService: OptimisticLockStockService,
) {

    @Transactional
    fun decrease(stockId: Long, quantity: Long) {
        while (true) {
            try {
                optimisticLockStockService.decrease(
                    stockId = stockId,
                    quantity = quantity
                )
                break
            } catch (e: Exception) {
                Thread.sleep(50)
            }
        }
    }
}
```
* @Transactional and @Transactional(propagation = Propagation.REQUIRES_NEW) 조합 가능
* None @Transactional + @Transactional 조합 가능
* 해당 케이스에 대해서 발생 사유 설명


## Pessimistic Lock


## Pessimistic Lock

## 정리할 내용
* Pessimistic Lock
* Optimistic Lock
* (정리 추가 1-1)
* 