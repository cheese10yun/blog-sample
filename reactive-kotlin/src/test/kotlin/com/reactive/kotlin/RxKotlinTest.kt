package com.reactive.kotlin

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.jupiter.api.Test
import java.util.Random

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
    internal fun `Observable create 메서드 이해`() {
        // Observer 생성
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

    }
}

