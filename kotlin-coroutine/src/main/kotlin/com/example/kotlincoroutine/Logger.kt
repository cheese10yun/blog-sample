package com.example.kotlincoroutine

import org.slf4j.LoggerFactory
import org.slf4j.Logger

inline fun <reified T> logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

inline fun kLogger(): Logger {
    return logger<GlobalLogger>()
}

object GlobalLogger