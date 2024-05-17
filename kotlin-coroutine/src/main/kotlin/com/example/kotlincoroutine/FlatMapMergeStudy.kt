package com.example.kotlincoroutine

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

class FlatMapMergeStudy {
    private val log by logger()
    private val orderClient: OrderClient = OrderClient()

    @OptIn(FlowPreview::class)
    suspend fun flatMapMergeWork(intRange: IntRange) {
        intRange
            .map { OrderRequest("$it") }
            .asFlow()
            .flatMapMerge {
                flow {
                    val aggregationKeys = mutableListOf<OrderResponse>()
                    orderClient
                        .getOrder(it)
                        .onFailure { log.error("Failure: $it") }
                        .onSuccess {
                            log.info("Success: $it")
                            aggregationKeys.add(it)
                        }
                    emit(aggregationKeys)
                }
            }
            .toList()

    }

    fun flatMapMergeWork2(intRange: IntRange) {
        intRange
            .map { OrderRequest("$it") }
            .map {
                orderClient.getOrder(it)
                    .onFailure { log.error("Failure: $it") }
                    .onSuccess {
                        log.info("Success: $it")
                    }
            }
    }
}


data class OrderRequest(val productId: String)


class OrderClient {
    fun getOrder(orderRequest: OrderRequest): ResponseResult<OrderResponse> {
        runBlocking {
            delay(300)
        }

        return ResponseResult.Success(OrderResponse(orderRequest.productId))

    }
}

data class OrderResponse(
    val productId: String
)