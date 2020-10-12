package com.reactive.kotlin

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.jupiter.api.Test

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
            .map { isEven(it) }
            .subscribe { println("This number is ${((if (it) "Even" else "Odd"))}") }

        subject.onNext(4)
        subject.onNext(9)
    }

    fun isEven(n: Int): Boolean = ((n % 2) == 0)
}