package com.example.kotlincoroutine

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

class FlatMapMergeStudy(
    private val orderClient: OrderClient
) {

    @OptIn(FlowPreview::class)
    fun flatMapMergeWork() {
        (1..2)
            .map {
                OrderRequest("$it")
            }
            .asFlow()
            .flatMapMerge {
                flow {
                    val aggregationKeys = mutableListOf<OrderResponse>()
                    orderClient.getOrder(it)
                        .onSuccess {
                            aggregationKeys.add(it)
                        }

                    emit(aggregationKeys)
                }
            }
    }
}


data class OrderRequest(val productId: String)


class OrderClient() {


    fun getOrder(orderRequest: OrderRequest): ResponseResult<OrderResponse> {

        return ResponseResult.Success(OrderResponse(orderRequest.productId))

    }
}

data class OrderResponse(
    val productId: String
)