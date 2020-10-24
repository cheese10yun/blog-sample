package com.reactive.kotlin

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.Random
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

internal class RxKotlinTest {

    @Test
    internal fun `pull 메커니즘`() {
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
    internal fun `observable`() {
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
    internal fun `evn odd`() {
        val subject: Subject<Int> = PublishSubject.create()

        subject
            .map { it.isEven(it) }
            .subscribe { println("This number is ${((if (it) "Even" else "Odd"))}") }

        subject.onNext(4)
        subject.onNext(9)
    }

    fun Int.isEven(n: Int): Boolean = ((n % 2) == 0)

    @Test
    internal fun `람다 표현식`() {
        val sum = { x: Int, y: Int -> x + y }
        println("sum ${sum(12, 14)}")

        val anonymousMult = { x: Int -> (Random().nextInt(15) + 1) * x }
        println("random out ${anonymousMult(2)}")
    }

    @Test
    internal fun `highOrderFun test`() {
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
    internal fun `observer`() {
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

        object : Observer<Int> {
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


    }
}

