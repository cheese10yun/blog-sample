package com.example.kotlincoroutine

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object JsonSupport {

    /**
     * JSON을 minify 진행한다. 불필요한 공백, 개행 및 탭 문자를 제거하여 JSON 크기를 줄인다.
     *
     * @param json minify 이전 JSON
     * ```
     * {
     *   "name": "xxx",
     *   "address": {
     *     "address": "address",
     *     "address_detail": "address_detail_1",
     *     "zip_code": "111"
     *   }
     * }
     * ```
     *
     * @return minify 이후 JSON
     * ```
     * {"name":"xxx","address":{"address":"address","address_detail":"address_detail_1","zip_code":"111"}}
     * ```
     *
     * @sample com.example.kotlincoroutine.JsonSupportTest.minifyJson
     */
    fun minifyJson(json: String): String {
        val result = StringBuilder()
        var insideString = false
        var escaped = false

        for (char in json) {
            when {
                escaped -> {
                    // 이스케이프된 문자는 그대로 추가
                    result.append(char)
                    escaped = false
                }
                char == '\\' -> {
                    // 이스케이프 문자 처리
                    result.append(char)
                    if (insideString) escaped = true
                }
                char == '"' -> {
                    // 따옴표 처리
                    insideString = !insideString
                    result.append(char)
                }
                insideString || !char.isWhitespace() -> {
                    // 문자열 안이거나 공백이 아닌 문자는 그대로 추가
                    result.append(char)
                }
            }
        }
        return result.toString()
    }
}

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }