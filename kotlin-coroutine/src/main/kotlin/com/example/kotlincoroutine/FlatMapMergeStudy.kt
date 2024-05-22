package com.example.kotlincoroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class FlatMapMergeStudy {
    private val log by logger()
    private val orderClient: OrderClient = OrderClient()

    @OptIn(FlowPreview::class)
    suspend fun getOrderFlow(orderRequests: List<OrderRequest>): List<OrderResponse> {
        return orderRequests
            .asFlow()
            .flatMapMerge { request ->
                flow {
                    orderClient
                        .getOrder(request)
                        .onFailure { log.error("Failure: $it") }
                        .onSuccess {
//                            log.info("Success: $it")
                            emit(it)
                        }
                }
            }
            .toList()
    }

    @OptIn(FlowPreview::class)
    suspend fun getOrderFlow2(orderRequests: List<OrderRequest>, concurrency: Int): List<OrderResponse> {
        log.info("===================")
        log.info("concurrency: $concurrency")
        log.info("===================")

        val currentActiveCoroutines = AtomicInteger(0)
        val maxActiveCoroutines = AtomicInteger(0)

        return orderRequests
            .asFlow()
            .flatMapMerge(concurrency) { request -> // 동시 실행할 코루틴 수 제한
                flow {

                    val activeCoroutines = currentActiveCoroutines.incrementAndGet()
                    maxActiveCoroutines.updateAndGet { max -> max.coerceAtLeast(activeCoroutines) }
                    log.info("Current active coroutines: $activeCoroutines")
                    orderClient
                        .getOrder(request)
                        .onFailure { log.error("Failure: $it") }
                        .onSuccess {
//                            log.info("Success: $it")
                            emit(it)
                        }

                    currentActiveCoroutines.decrementAndGet()
                }
            }
            .toList()
            .also {
                log.info("Max active coroutines: ${maxActiveCoroutines.get()}")
            }
    }

    @OptIn(FlowPreview::class)
    suspend fun getOrderFlow4(orderRequests: List<OrderRequest>, concurrency: Int): List<OrderResponse> {
        log.info("===================")
        log.info("concurrency: $concurrency")
        log.info("===================")

        return orderRequests
            .asFlow()
            .flatMapMerge(concurrency) { request -> // 동시 실행할 코루틴 수 제한
                flow {
                    orderClient
                        .getOrder(request)
                        .onFailure { log.error("Failure: $it") }
                        .onSuccess {
//                            log.info("Success: $it")
                            emit(it)
                        }

                }
            }
            .toList()
    }

    fun getOrderSync(orderRequests: List<OrderRequest>): List<OrderResponse> {
        return orderRequests
            .map {
                orderClient
                    .getOrder(it)
                    .onFailure { log.error("Failure: $it") }
                    .onSuccess { log.info("Success: $it") }
                    .getOrThrow()
            }
    }


    @OptIn(FlowPreview::class)
    suspend fun getOrderFlow3(orderRequests: List<OrderRequest>, concurrency: Int): List<OrderResponse> {
        val currentActiveCoroutines = AtomicInteger(0)
        val maxActiveCoroutines = AtomicInteger(0)
        val threadNames = mutableSetOf<String>()

        return orderRequests
            .asFlow()
            .flatMapMerge(concurrency = concurrency) { request -> // 동시 실행할 코루틴 수 제한
                flow {
                    val activeCoroutines = currentActiveCoroutines.incrementAndGet()
                    maxActiveCoroutines.updateAndGet { max -> max.coerceAtLeast(activeCoroutines) }

                    val threadName = Thread.currentThread().name
                    synchronized(threadNames) {
                        threadNames.add(threadName)
                    }

                    log.info("Current active coroutines: $activeCoroutines on thread $threadName")

                    val response = withContext(Dispatchers.IO) {
                        orderClient.getOrder(request)
                    }
                    response.onFailure { log.error("Failure: $it") }
                    response.onSuccess {
//                        log.info("Success: $it")
                        emit(it)
                    }

                    currentActiveCoroutines.decrementAndGet()
                }
            }
            .toList()
            .also {
                log.info("Max active coroutines: ${maxActiveCoroutines.get()}")
//                log.info("Threads used: $threadNames")
            }
    }
}


data class OrderRequest(val productId: String)


class OrderClient {
    fun getOrder(orderRequest: OrderRequest): ResponseResult<OrderResponse> {
        return runBlocking {
//            delay(300)
            ResponseResult.Success(OrderResponse(orderRequest.productId))
        }
    }
}

data class OrderResponse(
    val productId: String
)