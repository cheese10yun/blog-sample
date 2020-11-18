package com.rx.reactive

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class SampleApi {

    /**
     * 외부 IO 작업을 진행한다.
     * block은 500 ms
     * 80% 성공한다
     */
    fun doSomething(): Boolean {
        runBlocking {
            delay(100)
        }
        val random = Random.nextInt(0, 10)
        return 8 > random
    }
}