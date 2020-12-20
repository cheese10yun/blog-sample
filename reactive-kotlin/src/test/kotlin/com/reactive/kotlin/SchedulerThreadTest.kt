package com.reactive.kotlin

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File


class SchedulerThreadTest {

    @Test
    internal fun name() {

        val files = File("src/test/kotlin/com/reactive/kotlin").listFiles()

        Observable.fromArray(*files)
            .doOnNext { data -> Logger.log(LogType.DO_ON_NEXT, data) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .filter { data -> data.isDirectory }
            .map { dir -> dir.name }
            .subscribe { data -> Logger.log(LogType.ON_NEXT, data) }


        TimeUtil.sleep(2000L)

//        runBlocking { delay(2000L) }
    }

    @Test
    internal fun asdasd() {
        val subject = AsyncSubject.create<Int>()
        subject.onNext(1000)

        subject.doOnNext { price: Int ->
            Logger.log(
                LogType.DO_ON_NEXT,
                "# 소비자 1 : $price"
            )
        }
            .subscribe { price: Int ->
                Logger.log(
                    LogType.ON_NEXT,
                    "# 소비자 1 : $price"
                )
            }
        subject.onNext(2000)

        subject.doOnNext { price: Int ->
            Logger.log(
                LogType.DO_ON_NEXT,
                "# 소비자 2 : $price"
            )
        }
            .subscribe { price: Int ->
                Logger.log(
                    LogType.ON_NEXT,
                    "# 소비자 2 : $price"
                )
            }
        subject.onNext(3000)

        subject.doOnNext { price: Int ->
            Logger.log(
                LogType.DO_ON_NEXT,
                "# 소비자 3 : $price"
            )
        }
            .subscribe { price: Int ->
                Logger.log(
                    LogType.ON_NEXT,
                    "# 소비자 3 : $price"
                )
            }
        subject.onNext(4000)

        subject.onComplete()

        subject.doOnNext { price: Int ->
            Logger.log(
                LogType.DO_ON_NEXT,
                "# 소비자 4 : $price"
            )
        }
            .subscribe { price: Int ->
                Logger.log(
                    LogType.ON_NEXT,
                    "# 소비자 4 : $price"
                )
            }
    }
}