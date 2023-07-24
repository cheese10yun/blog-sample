package com.example.kotlincoroutine

import kotlin.system.measureTimeMillis
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class Coroutines {

    @Test
    @DelicateCoroutinesApi
    fun `코루틴 생성 테스트`() = runBlocking {
        println("${Thread.activeCount()} thread active at the start")

        val time = measureTimeMillis {
            createCoroutines(3)
        }

        println("${Thread.activeCount()} thread active at the end")
        println("Took $time ms")

    }

    @DelicateCoroutinesApi
    suspend fun createCoroutines(amount: Int) {
        val jobs = ArrayList<Job>()
        for (i in 1..amount) {
            jobs += GlobalScope.launch {
                println("Started $i in ${Thread.currentThread().name}")
                delay(1_000)
                println("Finished $i in ${Thread.currentThread().name}")
            }
        }
        jobs.forEach { it.join() }
    }

    lateinit var user: UserInfo

    @Test
    fun `레디스 컨디션`() = runBlocking {
        asyncGetUserInfo(1)
        delay(1000)
        println("User ${user.id} is ${user.name}")
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun asyncGetUserInfo(id: Int) = GlobalScope.async {
        delay(11000)
        user = UserInfo()
    }

    data class UserInfo(
        val id: Int = 1,
        val name: String = "Tster"
    )

    var counter = 0

    @Test
    fun `원자성 위반`() = runBlocking {
        val workerA = asyncIncrement(2_000)
        val workerB = asyncIncrement(100)

        workerA.await()
        workerB.await()
        println("counter [$counter]")

    }

    @OptIn(DelicateCoroutinesApi::class)
    fun asyncIncrement(by: Int) = GlobalScope.async {
        for (i in 0 until by)
            counter++
    }

    lateinit var jobA: Job
    lateinit var jobB: Job

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `교착 상태`() = runBlocking {
        GlobalScope.launch {
            delay(1_000)
            // wait for JobB to Fish
            jobB.join()
        }

        GlobalScope.launch {
            // wait for JobA to Fish
            jobA.join()
        }

        // wait for JobA to Fish
        jobA.join()
        println("Finished")
    }

    @Test
    fun `명시적 선언`() = runBlocking {
        val time = measureTimeMillis {
            val name = async { getName() }
            val lastName = async { getLastName() }
            println("Hello ${name.await()} ${lastName.await()}")
        }
        println("Execution took $time ms")
    }

    private suspend fun getName(): String {
        delay(1_000)
        return "Yun"
    }

    private suspend fun getLastName(): String {
        delay(1_000)
        return "Kim"
    }

//    suspend fun getProfile(id: Int){
//        val basicUserInfo = asyncGetUserInfo(id)
//        val contactInfo = asyncGetContactInfo(id)
//
//        createProfile(basicUserInfo.await(), contactInfo.wait())
//    }


}