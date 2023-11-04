package com.example.kotlincoroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


data class Member(val id: Int)

fun main() = runBlocking {
    val members = List(100) { Member(it) } // 100개의 멤버 생성

    // 병렬 나눠서 처리
    val size = members.size
    members
        .chunked(chunkSize(size))
        .forEach { chunk ->
            launch(Dispatchers.Default) {
                for (member in chunk) {
                    println("처리 중: ${member.id}, 스레드: ${Thread.currentThread().name}")
                }
            }
        }
}

private fun chunkSize(size: Int) = size / Runtime.getRuntime().availableProcessors()