package com.reactive.kotlin

import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

object DateUtil {

    val nowDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(Calendar.getInstance().time)
}

object TimeUtil {
    var start: Long = 0
    var end: Long = 0
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    fun start(): Long {
        start = System.currentTimeMillis()
        return start
    }

    fun end() {
        end = System.currentTimeMillis()
    }

    fun takeTime() {
        println("# 실행시간: " + (end - start) + " ms")
    }

    val currentTimeFormatted: String
        get() = LocalTime.now().format(formatter)
    val currentTime: Long
        get() = System.currentTimeMillis()

    fun sleep(interval: Long) {
        try {
            Thread.sleep(interval)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}

enum class LogType(val logType: String) {
    ON_SUBSCRIBE("onSubscribe()"),
    ON_NEXT("onNext()"),
    ON_ERROR("onERROR()"),
    ON_COMPLETE("onComplete()"),
    ON_SUCCESS("onSuccess()"),
    DO_ON_SUBSCRIBE("doOnSubscribe()"),
    DO_ON_NEXT("doOnNext()"),
    DO_ON_COMPLETE("doOnComplete()"),
    DO_ON_EACH("doOnEach()"),
    DO_ON_DISPOSE("doOnDispose()"),
    DO_ON_ERROR("donOnError()"),
    PRINT("print()");

}

object Logger {
    fun log(logType: LogType) {
        val time: String = TimeUtil.currentTimeFormatted
        println(logType.logType + " | " + Thread.currentThread().name + " | " + time)
    }

    fun log(logType: LogType, obj: Any) {
        val time: String = TimeUtil.currentTimeFormatted
        println(logType.logType + " | " + Thread.currentThread().name + " | " + time + " | " + obj)
    }
}
