package com.example.kotlincoroutine

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
                kotlinx.coroutines.delay(1_000)
                println("Finished $i in ${Thread.currentThread().name}")
            }
        }
        jobs.forEach { it.join() }
    }

    lateinit var user: UserInfo

    @Test
    fun `레디스 컨디션`() = runBlocking {
        asyncGetUserInfo(1)
        kotlinx.coroutines.delay(1000)
        println("User ${user.id} is ${user.name}")
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun asyncGetUserInfo(id: Int) = GlobalScope.async {
        kotlinx.coroutines.delay(11000)
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
            kotlinx.coroutines.delay(1_000)
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
        kotlinx.coroutines.delay(1_000)
        return "Yun"
    }

    private suspend fun getLastName(): String {
        kotlinx.coroutines.delay(1_000)
        return "Kim"
    }

//    suspend fun getProfile(id: Int){
//        val basicUserInfo = asyncGetUserInfo(id)
//        val contactInfo = asyncGetContactInfo(id)
//
//        createProfile(basicUserInfo.await(), contactInfo.wait())
//    }


    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun name() {
        newSingleThreadContext(name = "serviceCall")
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `async() 사용 방법`() = runBlocking {


        val task = GlobalScope.async {
            doSomething()
        }

//        task.join() // 예외가 발생해도, Completed 출력
        task.await() // 예외가 발생하면 종료
        println("Completed")
    }

    @OptIn(DelicateCoroutinesApi::class, InternalCoroutinesApi::class)
    @Test
    fun `async() 안전하게 예외 처리`() = runBlocking {
        val task = GlobalScope.async {
            doSomething()
        }

        task.join()
        if (task.isCancelled) {
            val exception = task.getCancellationException()
            println("Error with message: ${exception.cause}")
        } else {
            println("Success")
        }

        println("Completed")
    }

    @Test
    fun `코루틴에서 예외가 발생하는 경우`() {
        runBlocking { doSomethingTest() }
    }

    suspend fun doSomethingTest() = coroutineScope {
        val async = async { doSomething() }
        async.await()
        println("Completed")
    }

    private fun doSomething() {
        throw UnsupportedOperationException("Can`t Do")
    }


    @OptIn(InternalCoroutinesApi::class)
    @Test
    fun `launch`() = runBlocking {
        val task = GlobalScope.launch {
            doSomething()
        }
        task.join()
        if (task.isCancelled) {
            val exception = task.getCancellationException()
            println("Error with message: ${exception.cause}")
        } else {
            println("Success")
        }

        println("Completed")
    }

    @Test
    fun `특정 디스패처 사용하기`() = runBlocking {
        val task = launch {
            printCurrentThread()
        }
        task.join()
    }

    @Test
    fun `특정 디스패처 사용하기2`() = runBlocking {
        val dispatcher = newSingleThreadContext("ServiceCall")
        val task = launch(dispatcher) {
            printCurrentThread()
        }
        task.join()
    }

    fun printCurrentThread() {
        println("Running is thread [${Thread.currentThread().name}]")
    }

    @Test
    fun `Job 사용 방법`() = runBlocking {
        val job = GlobalScope.launch {
            // Do something..
        }
        // val job = Job() // 팩토리 함수로 사용 가능
    }

    @Test
    fun `Job 사용 방법 테스트`(): Unit = runBlocking {
        val job = GlobalScope.launch {
            TODO("Not Implemented!")
        }
        job.start()
//        job.join()
    }

    @Test
    fun `Job 예외 처리`() = runBlocking {
        val job = GlobalScope.launch {
            // Do something..
            TODO("Not Implemented!")
        }
        kotlinx.coroutines.delay(500)
    }

    @Test
    fun `생성 CoroutineStart LAZY`() = runBlocking {
        GlobalScope.launch(start = CoroutineStart.LAZY) {
            TODO("Not Implemented!")
        }
        kotlinx.coroutines.delay(500)
    }

    @Test
    fun `활성 CoroutineStart LAZY start`() = runBlocking {
        val job = GlobalScope.launch(start = CoroutineStart.LAZY) {
            kotlinx.coroutines.delay(3000)
        }
        job.start()
    }

    @Test
    fun `활성 CoroutineStart LAZY join`() = runBlocking {
        val job = GlobalScope.launch(start = CoroutineStart.LAZY) {
            kotlinx.coroutines.delay(3000)
        }
        job.join()
    }

    @Test
    fun `cancel `() = runBlocking {
        val job = GlobalScope.launch(start = CoroutineStart.LAZY) {
            // Do some work here
            kotlinx.coroutines.delay(5000)
        }
        kotlinx.coroutines.delay(2000)
        job.cancel()
    }

    @Test
    fun `cancel cause`() = runBlocking {
        val job = GlobalScope.launch(start = CoroutineStart.LAZY) {
            // Do some work here
            kotlinx.coroutines.delay(5000)
        }
        kotlinx.coroutines.delay(2000)
        job.cancel(cause = CancellationException("Timeout"))
    }

    @OptIn(InternalCoroutinesApi::class)
    @Test
    fun `cancel getCancellationException`() = runBlocking {
        val job = GlobalScope.launch(start = CoroutineStart.LAZY) {
            // Do some work here
            kotlinx.coroutines.delay(5000)
        }
        kotlinx.coroutines.delay(2000)
        job.cancel(cause = CancellationException("Tried of waiting"))

        val cancellation = job.getCancellationException()
        println(cancellation)
    }

    @Test
    fun `cancel CoroutineException Handler`() = runBlocking {
        val exceptionHandler = CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
            println("Job cancelled due to ${throwable.message}")
        }
        GlobalScope.launch(exceptionHandler) {
            TODO("Not implemented yet!")
        }
        kotlinx.coroutines.delay(2000)
    }

    @Test
    fun `cancel invokeOnCompletion`() = runBlocking {
        GlobalScope.launch {
            TODO("Not implemented yet!")
        }.invokeOnCompletion { cause ->
            cause?.let {
                println("Job canelled due to ${it.message}")
            }
        }
        kotlinx.coroutines.delay(2000)
    }


    @Test
    fun `Deferred handler`() = runBlocking {
        val headlinesTask = GlobalScope.async {
            getHeadLines()
        }
        headlinesTask.await()
    }

    private fun getHeadLines() {
        // Do some work here
    }


    @Test
    fun `Deferred 예외 처리`(): Unit = runBlocking {
        val deferred = GlobalScope.async {
            TODO("Not implemented yet!")
        }

        // Wait for it to fail
//        delay(2_000)
        deferred.await()
    }

    @Test
    fun `Deferred 예외 처리 try-catch`(): Unit = runBlocking {
        val deferred = GlobalScope.async {
            TODO("Not implemented yet!")
        }
        try {
            deferred.await()
        } catch (e: Throwable) {
            println(e.printStackTrace())
        }
    }

    @Test
    fun `상태는 한 방향으로만 이동`() = runBlocking {
        val time = measureTimeMillis {
            val job = GlobalScope.launch {
                kotlinx.coroutines.delay(2000)
            }
            job.join()

            // Restart the job
            job.start()
            job.join()
        }
        println("Took $time ms")
    }

    @Test
    fun `suspend 외부`() {
        runBlocking {
            greetDelayed(1000)
        }
    }

    suspend fun greetDelayed(delayMillis: Long) {
        kotlinx.coroutines.delay(delayMillis)
        println("Hello, World!")
    }

    data class Profile(
        val id: Long,
        val name: String,
        val age: Int
    )

//    interface ProfileServiceRepository {
//        fun fetchByName(name:String): Profile
//
//        fun fetchById(id: Long): Profile
//    }

//    interface ProfileServiceRepository {
//        fun asyncFetchByName(name:String): Deferred<Profile>
//        fun asyncFetchById(id: Long): Deferred<Profile>
//    }

    interface ProfileServiceRepository {
        suspend fun fetchByName(name: String): Profile
        suspend fun fetchById(id: Long): Profile
    }

    class ProfileServiceClient : ProfileServiceRepository {
        override suspend fun fetchByName(name: String): Profile {
            return Profile(1, name, 28)
        }

        override suspend fun fetchById(id: Long): Profile {
            return Profile(1, "name", 28)
        }
    }

    @Test
    fun `구현 테스트 코드`() = runBlocking {
        val client = ProfileServiceClient()

        val profile = client.fetchById(12)
        println(profile)
    }

    @Test
    fun `CommonPool test`() {

        GlobalScope.launch(Dispatchers.Default) {

        }
    }

    @Test
    fun `Unconfined test`() = runBlocking {
        GlobalScope.launch(Dispatchers.Unconfined) {
            println("Starting in ${Thread.currentThread().name}")
            kotlinx.coroutines.delay(500)
            println("Resuming in ${Thread.currentThread().name}")
        }.join()
    }


    @Test
    fun `newSingleThreadContext test `() = runBlocking {
        val dispatcher = newSingleThreadContext("myThread")
        GlobalScope.launch(dispatcher) {
            println("Starting in ${Thread.currentThread().name}")
            kotlinx.coroutines.delay(500)
            println("Resuming in ${Thread.currentThread().name}")
        }.join()
    }

    @Test
    fun `newFixedThreadPoolContext test`() = runBlocking {
        val dispatcher = newFixedThreadPoolContext(4, "myPool")
        GlobalScope.launch(dispatcher) {
            println("Starting in ${Thread.currentThread().name}")
            kotlinx.coroutines.delay(500)
            println("Resuming in ${Thread.currentThread().name}")
        }.join()
    }

    @Test
    fun `CoroutineExceptionHanlder test`() = runBlocking {
        val handler = CoroutineExceptionHandler { context, throwable ->
            println("Error capured is $context")
            println("Message: ${throwable.message}")
        }

        GlobalScope.launch(handler) {
            TODO()
        }

        // wait for error to happen
        kotlinx.coroutines.delay(500)
    }

    @Test
    fun `non-cancellable`() = runBlocking {
        val duration = measureTimeMillis {
            val job = launch {
                try {
                    while (isActive) {
                        kotlinx.coroutines.delay(500)
                        println("still running")
                    }
                } finally {
                    println("cancelled, will end now")
                }
            }
            kotlinx.coroutines.delay(1200)
            job.cancelAndJoin()
        }
        println("Took $duration ms")
    }

    @Test
    fun `non-cancellable 2`() = runBlocking {
        val duration = measureTimeMillis {
            val job = launch {
                try {
                    while (isActive) {
                        kotlinx.coroutines.delay(500)
                        println("still running")
                    }
                } finally {
                    println("cancelled, will delay finalization now")
                    kotlinx.coroutines.delay(5000)
                    println("delay completed, bye bye")
                }
            }
            kotlinx.coroutines.delay(1200)
            job.cancelAndJoin()
        }
        println("Took $duration ms")
    }

    @Test
    fun `non-cancellable 3`() = runBlocking {
        val duration = measureTimeMillis {
            val job = launch {
                try {
                    while (isActive) {
                        kotlinx.coroutines.delay(500)
                        println("still running")
                    }
                } finally {
                    withContext(NonCancellable) {
                        println("cancelled, will delay finalization now")
                        kotlinx.coroutines.delay(5000)
                        println("delay completed, bye bye")
                    }
                }
            }
            kotlinx.coroutines.delay(1200)

            job.cancelAndJoin()
        }
        println("Took $duration ms")
    }


    @Test
    fun `컨텍스트 조합`(): Unit = runBlocking {
        val dispatcher = newSingleThreadContext("MyDispatcher")
        val handler = CoroutineExceptionHandler { context, throwable ->
            println("Error captured")
            println("Message: ${throwable.message}")
        }

        GlobalScope.launch(dispatcher + handler) {
            println("Running in ${Thread.currentThread().name}")
            TODO("Not Implements")
        }.join()
    }

    @Test
    fun `컨텍스트 분리`(): Unit = runBlocking {
        val dispatcher = newSingleThreadContext("MyDispatcher")
        val handler = CoroutineExceptionHandler { context, throwable ->
            println("Error captured")
            println("Message: ${throwable.message}")
        }

        // 두 컨텍스트를 결합
        val context = dispatcher + handler

        // 컨텍스트예서 하나의 요소 제거
        val tmpCtx = context.minusKey(dispatcher.key)

        GlobalScope.launch(tmpCtx) {
            println("Running in ${Thread.currentThread().name}")
            TODO("Not Implements")
        }.join()
    }

    @Test
    fun `withContext()를 사용하는 임시 컨텍스트 스위치`(): Unit = runBlocking {
        val dispatcher = newSingleThreadContext("MyThread")

        val name = GlobalScope.async(dispatcher) {
            // 중요한 작업 수행
            "Susan Calvin"
        }.await()

        println("User: $name")
    }

    @Test
    fun `withContext()를 사용하는 임시 컨텍스트 스위치2`(): Unit = runBlocking {
        val dispatcher = newSingleThreadContext("MyThread")
        val name = withContext(dispatcher) {
            "Susan Calvin"
        }

        println("User: $name")
    }

    @Test
    fun `이터레이터`() {
        val iterator = iterator {
            yield("First")
            yield("Second")
            yield("Third")
        }

        println(iterator.next())
        println(iterator.next())
        println(iterator.next())
    }

    @Test
    fun `시퀀스`() {
        val sequence = sequence {
            yield(1)
            yield(1)
            yield(2)
            yield(3)
            yield(5)
            yield(8)
            yield(13)
            yield(21)
        }

        sequence.forEach { print("$it ") }
    }


    @Test
    fun `test`() = runBlocking {
        val time = measureTimeMillis {
            val dispatcher = newSingleThreadContext("asd")
            val job1 = launch(Dispatchers.Default) { delay(1_000) }
            val job2 = launch(Dispatchers.Default) { delay(10) }

            job1.join()
            job2.join()


        }
        println("Execution took $time ms")
    }

    private suspend fun delay(delay: Long): String {
        kotlinx.coroutines.delay(delay)
        println("delay:${delay} Thread ${Thread.currentThread().name}")
        return "Yun"
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `프로듀서 만들기`() {
        val producer = GlobalScope.produce {
            send(1)
        }

        val context = newSingleThreadContext("myThread")
        val producer1: ReceiveChannel<Any> = GlobalScope.produce(context) {
            send(1)
            send("a")
        }
    }

    val context = newSingleThreadContext("myThread")
    val produce = GlobalScope.produce(context) {
        for (i in 0..9) {
            send(i)
        }
    }
    @Test
    fun `프로듀서 모든 요소 읽기`() {
        runBlocking {
            produce.consumeEach {
                println(it)
            }
        }
    }

    @Test
    fun `단일 요소 받기`() {
        val produce = GlobalScope.produce {
            send(5)
            send("a")
        }

        runBlocking {
            println(produce.receive())
            println(produce.receive())
        }
    }

    @Test
    fun `그룹 요소 가져 오기`() {
        val produce = GlobalScope.produce {
            send(5)
            send("a")
        }

        runBlocking {
            produce.consumeEach {
                println(it)
            }
        }

    }
}