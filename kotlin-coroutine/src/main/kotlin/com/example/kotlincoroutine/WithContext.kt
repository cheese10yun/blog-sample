//package com.example.kotlincoroutine
//
//import kotlinx.coroutines.CoroutineName
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.withContext
//
//private val log = kLogger()
//
//fun main(): Unit = runBlocking {
//    log.info("start context in runBlocking: ${this.coroutineContext}")
//    withContext(CoroutineName("withContext")) {
//        log.info("context in withContext: ${this.coroutineContext}")
//
//        launch {
//            delay(200)
//            log.info("launch in withContext")
//        }
//    }
//
//    val launch = launch {
//        delay(50)
//        log.info("launch in runBlocking")
//    }
//    launch.join()
//
//    log.info("end context in runBlocking: ${this.coroutineContext}")
//}