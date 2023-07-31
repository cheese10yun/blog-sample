package com.example.kotlincoroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

@OptIn(FlowPreview::class)
fun main() = runBlocking {
    // 1000개의 List<Int>를 생성합니다
    val listOfLists = (1..7).map { it }

    // CPU 코어 수에 맞게 parallelism을 설정합니다
    val parallelism = Runtime.getRuntime().availableProcessors()
    println(parallelism)

    // 각 요소를 병렬로 처리하고 결과를 병합하는 Flow를 생성합니다
    val concurrency = DEFAULT_CONCURRENCY
    val mergedResultFlow: Flow<Int> = listOfLists.asFlow()
        .flatMapMerge(concurrency) { item ->
            flow {
                val processedItem = processItem(item)
                logThread("flow")
                emit(processedItem)
            }
                .flowOn(Dispatchers.Default) // 각 코루틴이 별도의 스레드에서 실행되도록 설정합니다
        }

    // 처리된 결과를 수집합니다
    val mergedResult = mergedResultFlow.toList()

    // 결과를 출력합니다
//    println("병합된 결과: $mergedResult")
}

fun logThread(message: String) {
    println("$message on thread: ${Thread.currentThread().name}")
}

suspend fun processItem(item: Int): Int {
    // 시간이 오래 걸리는 처리를 시뮬레이션합니다
//    kotlinx.coroutines.delay(100)
    return item * 2
}