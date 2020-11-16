package com.reactive.kotlin

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.Optional
import java.util.Random
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class RxKotlinTest {

    @Test
    fun `pull 메커니즘`() {
        val list = listOf(
            "one",
            2,
            "Three",
            "Four",
            4.5,
            "Five",
            6.0f
        )
        val iterator = list.iterator()

        while (iterator.hasNext()) {
            println(iterator.next())
        }
    }

    @Test
    fun `observable`() {
        val list = listOf(
            "one",
            2,
            "Three",
            "Four",
            4.5,
            "Five",
            6.0f
        )

        val observable = list.toObservable()

        observable.subscribeBy(
            onNext = { println(it) },
            onError = { it.printStackTrace() },
            onComplete = { println("Done!") }
        )
    }

    @Test
    fun `evn odd`() {
        val subject: Subject<Int> = PublishSubject.create()

        subject
            .map { it.isEven(it) }
            .subscribe { println("This number is ${((if (it) "Even" else "Odd"))}") }

        subject.onNext(4)
        subject.onNext(9)
    }

    fun Int.isEven(n: Int): Boolean = ((n % 2) == 0)

    @Test
    fun `람다 표현식`() {
        val sum = { x: Int, y: Int -> x + y }
        println("sum ${sum(12, 14)}")

        val anonymousMult = { x: Int -> (Random().nextInt(15) + 1) * x }
        println("random out ${anonymousMult(2)}")
    }

    @Test
    fun `highOrderFun test`() {
        highOrderFun(2, { a: Int -> a.isEven(2) })
    }

    fun highOrderFun(a: Int, validityCheckFunc: (a: Int) -> Boolean) {
        if (validityCheckFunc(2)) {
            println("a $a is Valid")
        } else {
            println("a $a is Invalid")
        }
    }

    @Test
    fun `observer`() {
        val observer: Observer<Any> = object : Observer<Any> {
            override fun onComplete() {
                println("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                println("onSubscribe: $d")
            }

            override fun onError(e: Throwable) {
                println("onError: $e")
            }

            override fun onNext(item: Any) {
                println("onNext: $item")
            }
        }

        val observable: Observable<Any> = listOf("One", 2, "Three", "Four", 4.5, "Five", 6.0f).toObservable() //6

        observable.subscribe(observer)//7

        val observableOnList: Observable<List<Any>> = Observable.just(
            listOf("One", 2, "Three", "Four", 4.5, "Five", 6.0f),
            listOf("List with Single Item"),
            listOf(1, 2, 3, 4, 5, 6)
        )//8

        observableOnList.subscribe(observer)//9
    }

    @Test
    fun `Observable create 메서드 이해`() {
        // Observer 생성
        val observable = object : Observer<Any> {
            override fun onComplete() {
                println("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                println("onSubscribe: $d")
            }

            override fun onError(e: Throwable) {
                println("onError: $e")
            }

            override fun onNext(item: Any) {
                println("onNext: $item")
            }
        }

        val observable1 = Observable.create<String> {
            it.onNext("Emit 1")
            it.onNext("Emit 2")
            it.onNext("Emit 3")
            it.onNext("Emit 4")
            it.onComplete()
        }

        observable1.subscribe(observable)

        val observable2 = Observable.create<String> {
            it.onNext("Emit 1")
            it.onNext("Emit 2")
            it.onNext("Emit 3")
            it.onNext("Emit 4")
            it.onError(Exception("Custom Exception"))
        }

        observable2.subscribe(observable)
    }

    @Test
    fun `Observable from 메서드 이해`() {
        // Observer 생성
        val observer = object : Observer<Any> {
            override fun onComplete() {
                println("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                println("onSubscribe: $d")
            }

            override fun onError(e: Throwable) {
                println("onError: $e")
            }

            override fun onNext(item: Any) {
                println("onNext: $item")
            }
        }

        val list = listOf("string 1", "string 2", "string 3", "string 4")

        val fromIterable = Observable.fromIterable(list)

        fromIterable.subscribe(observer)


        val callable = object : Callable<String> {
            override fun call(): String {
                return "From Callable"
            }
        }

        val fromCallable = Observable.fromCallable(callable)
        fromCallable.subscribe(observer)


        val future = object : Future<String> {
            override fun isDone(): Boolean = true

            override fun get(): String = "Hello From Future"

            override fun get(timeout: Long, unit: TimeUnit): String = "Hello From Future"

            override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false

            override fun isCancelled(): Boolean = false
        }

        val fromFuture = Observable.fromFuture(future)
        fromFuture.subscribe(observer)
    }

    @Test
    fun `toObserverable 확장 함수의 이해`() {
        // Observer 생성
        val observer = object : Observer<Any> {
            override fun onComplete() {
                println("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                println("onSubscribe: $d")
            }

            override fun onError(e: Throwable) {
                println("onError: $e")
            }

            override fun onNext(item: Any) {
                println("onNext: $item")
            }
        }

        val list = listOf("string 1", "string 2", "string 3", "string 4")

        val observable = list.toObservable()

        observable.subscribe(observer)
    }

    @Test
    fun `observerable just 함수의 이해`() {
        val observer = object : Observer<Any> {
            override fun onComplete() {
                println("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                println("onSubscribe: $d")
            }

            override fun onError(e: Throwable) {
                println("onError: $e")
            }

            override fun onNext(item: Any) {
                println("onNext: $item")
            }
        }

        Observable.just("A String").subscribe(observer)
        Observable.just(54).subscribe(observer)
        Observable.just(listOf("string 1", "string 2", "string 3", "string 4")).subscribe(observer)
        Observable.just(
            mapOf(
                Pair("Key 1", "Value1"),
                Pair("Key 2", "Value2"),
                Pair("Key 3", "Value3")
            )
        ).subscribe(observer)

        Observable.just("string 1", "string 2", "string 3", "string 4").subscribe(observer)
    }

    @Test
    fun `Observable의 다른팩토리 메서드`() {
        val observer = object : Observer<Any> {
            override fun onComplete() {
                println("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                println("onSubscribe: $d")
            }

            override fun onError(e: Throwable) {
                println("onError: $e")
            }

            override fun onNext(item: Any) {
                println("onNext: $item")
            }
        }

        Observable.range(0, 10).subscribe(observer) // (1)
        Observable.empty<String>().subscribe(observer) // (2)

        runBlocking {
            Observable.interval(300, TimeUnit.MILLISECONDS).subscribe(observer) // (3)
            delay(900)

            Observable.timer(400, TimeUnit.MILLISECONDS).subscribe(observer) // (4)
            delay(450)
        }
    }

    @Test
    fun `구독과 해지`() {
        val observable = Observable.range(1, 5)

        observable.subscribe(
            {
                println("Next $it")
            },
            {
                println("Error ${it.message}")
            },
            {
                println("Done")
            }
        )

        val observer = object : Observer<Int> {
            override fun onComplete() {
                println("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                println("onSubscribe")
            }

            override fun onNext(t: Int) {
                println("onNext: $Int")
            }

            override fun onError(e: Throwable) {
                println("onError: ${e.message}")
            }
        }

        observable.subscribe(observer)
    }

    @Test
    fun `구독과 해지2`() {
        runBlocking {
            val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
            val observer = object : Observer<Long> {
                lateinit var disposable: Disposable

                override fun onComplete() {
                    println("onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    println("onSubscribe")
                    disposable = d
                }

                override fun onNext(item: Long) {
                    println("onNext: $item")
                    if (item >= 10 && disposable.isDisposed.not()) {
                        disposable.dispose()
                        println("dispose")
                    }
                }

                override fun onError(e: Throwable) {
                    println("onError: ${e.message}")
                }
            }

            observable.subscribe(observer)
            delay(1500L)
        }
    }

    @Test
    fun `콜드 옵저버블`() {
        val observable = listOf("string1", "string2", "string3", "string4").toObservable()

        observable.subscribe(
            {
                println("Received $it")
            },
            {
                println("Error ${it.message}")
            },
            {
                println("Done")
            }
        )

        observable.subscribe(
            {
                println("Received $it")
            },
            {
                println("Error ${it.message}")
            },
            {
                println("Done")
            }
        )

    }

    @Test
    fun `핫 옵저버블`() {
        val connectableObservable = listOf("string1", "string2", "string3", "string4", "string5").toObservable().publish()

        connectableObservable.subscribe { println("Subscription 1: $it") } // 콜드 옵저버블
        connectableObservable.map(String::reversed).subscribe { println("Subscription 2: $it") } // 콜드 옵저버블
        connectableObservable.connect() // connect를 사용하여 콜드 옵저저블을 핫 옵저저블로 변경한다.
        connectableObservable.subscribe { println("Subscription 3: $it") }
    }

    @Test
    fun `핫 옵저버블2`() {
        val connectableObservable = Observable.interval(100, TimeUnit.MILLISECONDS).publish()

        connectableObservable.subscribe { println("Subscription 1: $it") } // 콜드 옵저버블
        connectableObservable.subscribe { println("Subscription 2: $it") }
        connectableObservable.connect() // connect를 사용하여 콜드 옵저저블을 핫 옵저저블로 변경한다.
        runBlocking { delay(500) }

        connectableObservable.subscribe { println("Subscription 3: $it") }
        runBlocking { delay(500) }
    }

    @Test
    fun subject() {
        val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
        val subject = PublishSubject.create<Long>()
        observable.subscribe(subject)

        subject.subscribe { println("Received $it") }
        runBlocking { delay(1100) }
    }

    @Test
    fun subject2() {
        val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
        val subject = PublishSubject.create<Long>()
        observable.subscribe(subject)

        subject.subscribe { println("Subscription 1 Received $it") }
        runBlocking { delay(1100) }

        subject.subscribe { println("Subscription 2 Received $it") }
        runBlocking { delay(1100) }
    }

    @Test
    fun subject3() {
//        val observable = listOf(1L, 2L, 3L, 4L, 5L).toObservable().publish()
        val observable = Observable.just(1L, 2L, 3L, 4L).publish()
//        val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
        val subject = PublishSubject.create<Long>()
        observable.subscribe(subject)

        subject.subscribe { println("Subscription 1 Received $it") }
        runBlocking { delay(1100) }

        subject.subscribe { println("Subscription 2 Received $it") }
        runBlocking { delay(1100) }
    }

    @Test
    fun `AsyncSubject 이해`() {
        val observable = Observable.just(1, 2, 3, 4)
        val subject = AsyncSubject.create<Int>()

        observable.subscribe(subject)
        subject.subscribe(
            {
                println("Received $it")
            },
            {
                it.printStackTrace()
            },
            {
                println("Done")
            }
        )
    }

    @Test
    fun `AsyncSubject 이해2`() {
        val observable = Observable.just(1, 2, 3, 4)
        val subject = AsyncSubject.create<Int>()

        observable.subscribe(subject)
        subject.subscribe(
            {
                println("Received $it")
            },
            {
                it.printStackTrace()
            },
            {
                println("Done")
            }
        )
    }

    @Test
    fun `BehaviorSubject 이해`() {
        val subject = BehaviorSubject.create<Int>()
        subject.onNext(1)
        subject.onNext(2)
        subject.onNext(3)
        subject.onNext(4)
        subject.subscribe(
            {
                println("S1 Received $it")
            },
            {
                it.printStackTrace()
            },
            {
                println("S1 Completed")
            }
        )
        subject.onNext(5)
        subject.subscribe(
            {
                println("S2 Received $it")
            },
            {
                it.printStackTrace()
            },
            {
                println("S2 Completed")
            }
        )
        subject.onComplete()
    }

    @Test
    fun `ReplaySubject 이해`() {
        val subject = ReplaySubject.create<Int>()
        subject.onNext(1)
        subject.onNext(2)
        subject.onNext(3)
        subject.onNext(4)
        subject.subscribe(
            {
                println("S1 Received $it")
            },
            {
                it.printStackTrace()
            },
            {
                println("S1 Completed")
            }
        )
        subject.onNext(5)
        subject.subscribe(
            {
                println("S2 Received $it")
            },
            {
                it.printStackTrace()
            },
            {
                println("S2 Completed")
            }
        )
        subject.onComplete()
    }

    @Test
    fun `백프레셔 이해`() {
        val observable = Observable.just(1, 2, 3, 4, 5, 6, 7, 9) // (1)
        val subject = BehaviorSubject.create<Int>()

        subject.observeOn(Schedulers.computation()) // (2)
            .subscribe {
                println("Sub 1 Received $it")
                runBlocking { delay(200) } // (4)
            }

        subject.observeOn(Schedulers.computation()) // (5)
            .subscribe {// (6)
                println("Sub 2 Received $it")
            }

        observable.subscribe(subject) // (7)
        runBlocking { delay(200) } // (8)
    }

    @Test
    fun `백프레셔 이해 2`() {
        val observable = Observable.just(1, 2, 3, 4, 5, 6, 7, 9) // (1)

        observable
            .map { MyItem(it) }
            .observeOn(Schedulers.computation())
            .subscribe {
                println("Received $it")
                runBlocking { delay(200) }
            }

        runBlocking { delay(2000) }
    }

    @Test
    fun `풀로어블`() {
        Observable.range(1, 1000) //(1)
            .map { MyItem(it) } //(2)
            .observeOn(Schedulers.io())
            .subscribe( //(3)
                {
                    println("Received $it")
                    runBlocking { delay(50) } //(4)
                },
                {
                    it.printStackTrace() //(5)
                }
            )
        runBlocking { delay(6000) }
    }

    @Test
    fun `flowable`() {
        Flowable.range(1, 1000)
            .map { MyItem(it) }
            .observeOn(Schedulers.io())
            .subscribe(
                {
                    println("Receivbed $it")
                    runBlocking { delay(50) }
                },
                {
                    it.printStackTrace()
                }
            )
        runBlocking { delay(6000) }
    }

    @Test
    fun subscriber() {

        Flowable.range(1, 15)
            .map { MyItem(it) }
            .observeOn(Schedulers.io())
            .subscribe(object : Subscriber<MyItem> {
                lateinit var subscription: Subscription

                override fun onSubscribe(subscription: Subscription) {
                    this.subscription = subscription
                    subscription.request(5)
                }

                override fun onNext(t: MyItem) {
                    runBlocking { delay(50) }
                    println("Subscriber received $t")

                    if (t.id == 5) {
                        println("Request two more")
                        subscription.request(2)
                    }
                }

                override fun onError(t: Throwable) = t.printStackTrace()

                override fun onComplete() = println("Done")
            })

        runBlocking { delay(10000) }
    }

    @Test
    fun `처음뿌터 플로어블 생성하기`() {
        val observer = object : Observer<Int> {
            override fun onSubscribe(d: Disposable) {
                println("New Subscription")
            }

            override fun onNext(item: Int) {
                println("item: $item")
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }

            override fun onComplete() {
                println("All Completed")
            }
        }

        val observable = Observable.create<Int> {
            for (i in 1..10) {
                it.onNext(i)
            }
            it.onComplete()
        }

        observable.subscribe(observer)
    }

    @Test
    internal fun `처음부터 플로어블 생성하기 2`() {
        val observer = object : Subscriber<Int> {
            override fun onNext(item: Int) {
                println("item: $item")
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }

            override fun onComplete() {
                println("All Completed")
            }

            override fun onSubscribe(subscription: Subscription) {
                println("New Subscription")
                subscription.request(10)
            }
        }
    }

    @Test
    fun `BackpressureStrategy ERROR`() {
        val source = Observable.range(1, 500)

        source.toFlowable(BackpressureStrategy.BUFFER)
//            .onBackpressureDrop { println("Dropped $it") }
            .onBackpressureBuffer()
//            .onBackpressureLatest()
            .map { MyItem(it) }
            .observeOn(Schedulers.io())
            .subscribe {
                println(it)
                runBlocking { delay(100) }
            }
        runBlocking { delay(10000) }
    }

    @Test
    fun `flowable generate`() {

        val flowable = Flowable.generate<Int>() {
            it.onNext(GenerateFlowableItem.item)
        }

        flowable
            .map { MyItem(it) }
            .subscribeOn(Schedulers.io())
            .subscribe {
                runBlocking { delay(10) }
                println("Next $it")
            }

        runBlocking { delay(7000) }
    }

    @Test
    fun `processor`() {
        val flowable = listOf("stinrg 1", "stirng 2", "string 3", "string 4").toFlowable()
        val processor = PublishProcessor.create<String>()

        processor
            .subscribe {
                println("Subscription 1 : $it")
                runBlocking { delay(1000) }
                println("Subscription 1 delay")
            }

        processor
            .subscribe { println("Subscription 2 : $it") }

        flowable.subscribe(processor)
    }

    data class MyItem(val id: Int) {
        init {
            println("MyItem Created $id")
        }
    }

    object GenerateFlowableItem {
        var item: Int = 0
            get() {
                field += 1
                return field
            }
    }

    @Test
    fun `distinct`() {

        val list = (1..300).map {
            1L
        }

        list
            .toFlowable()
//            .toObservable()
            .subscribeOn(Schedulers.io())
//            .distinct()
            .subscribe(
                {
                    println("Received $it")
                },
                {
                    println(it.printStackTrace())
                },
                {
                    println("completed")
                },
                {
                    println("subscrition")
                }
            )

        runBlocking { delay(7000) }
    }

    @Test
    fun `스케줄러 종류`() {

        Observable.range(1, 10)
            .subscribeOn(Schedulers.computation())
            .subscribe {
                runBlocking { delay(200) }
                println("Observable1 Item Received $it")
            }

        Observable.range(21, 10)
            .subscribeOn(Schedulers.computation())
            .subscribe {
                runBlocking { delay(200) }
                println("Observable2 Item Received $it")
            }

        runBlocking { delay(2100) }
    }

    @Test
    fun `subscribeOn 연산자`() {
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
            .toObservable()
            .subscribeOn(Schedulers.io())
            .map { item ->
                println("Mapping $item ${Thread.currentThread().name}")
                return@map item.toInt()
            }
            .observeOn(Schedulers.computation())
            .subscribe { item ->
                println("Received $item ${Thread.currentThread().name}")
            }

        runBlocking { delay(1000) }
    }

    @Test
    fun `subscribeOn 연산자2`() {


        val poolSize = Optional.ofNullable(System.getProperty("reactor.schedulers.defaultPoolSize"))
            .map { s: String -> s.toInt() }
            .orElseGet { Runtime.getRuntime().availableProcessors() }


        val subscribe = (1..10_000)
            .map { it }
            .toFlowable()
            .parallel()
            .runOn(Schedulers.io())
            .map { item ->
                println("Mapping $item ${Thread.currentThread().name}")
                return@map item
            }
            .sequential()
            .blockingSubscribe {
                println("Received $it ${Thread.currentThread().name}")
            }
//            .subscribe {
//                println("Received $it ${Thread.currentThread().name}")
//            }


        println("111")
    }

    @Test
    internal fun `current core count`() {
        val orElseGet = Optional.ofNullable(System.getProperty("reactor.schedulers.defaultPoolSize"))
            .map { s: String -> s.toInt() }
            .orElseGet { Runtime.getRuntime().availableProcessors() }

        println("===============")
        println(orElseGet)
        println("===============")
    }
}