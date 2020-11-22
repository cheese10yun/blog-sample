package com.rx.reactive

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

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