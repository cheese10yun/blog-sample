package com.example.api.filter

import org.springframework.http.MediaType
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * HTTP 요청 컨텐츠를 읽고 스트림을 리셋하여 다시 읽을 수 있도록 하는 [HttpServletRequest]의 래퍼
 */
internal class ContentReadingHttpRequestWrapper(request: HttpServletRequest) : ContentCachingRequestWrapper(request) {
    /**
     * HTTP 요청 컨텐츠에 대한 문자열 프로퍼티입니다.
     *
     * 컨텐츠 타입이 human-readable한 경우, 컨텐츠의 바이트 배열을 문자열로 변형하여 반환합니다.
     */
    val contentAsString: String by lazy {
        if ((contentType?.let { MediaType.valueOf(it) } ?: MediaType.ALL).isVisible()) {
            val charset =
                if (Charset.isSupported(characterEncoding)) Charset.forName(characterEncoding) else Charset.defaultCharset()

            String(contentAsByteArray, charset)
        } else {
            "*"
        }
    }

    override fun getContentAsByteArray(): ByteArray = contentAsBytes

    /**
     * HTTP 요청 컨텐츠에 대한 바이트 배열 프로퍼티입니다.
     *
     * 입력 스트림을 읽어 바이트 배열로 반환합니다.
     */
    private val contentAsBytes: ByteArray by lazy {
        ByteArrayOutputStream().use {
            val buffer = ByteArray(1024)
            var count = 0
            while (-1 != { count = super.getInputStream().read(buffer); count }()) {
                it.write(buffer, 0, count)
            }
            it.toByteArray()
        }
    }

    override fun getInputStream(): ServletInputStream = wrappedInputStream

    /**
     * [contentAsBytes] 메서드 실행 이후 요청 컨텐츠를 한번 더 읽을 수 있도록 [ServletInputStream]을 제공합니다.
     */
    private val wrappedInputStream: ServletInputStream by lazy {
        val inputStream = ByteArrayInputStream(contentAsBytes)
        object : ServletInputStream() {
            override fun isReady(): Boolean = false

            override fun isFinished(): Boolean = false

            override fun read(): Int = inputStream.read()

            override fun read(b: ByteArray): Int = inputStream.read(b)

            override fun setReadListener(listener: ReadListener?) {}
        }
    }
}

/**
 * HTTP 응답 컨텐츠를 읽고 스트림을 리셋하여 다시 읽을 수 있도록 하는 [HttpServletResponse]의 래퍼입니다.
 *
 */
class ContentReadingHttpResponseWrapper(response: HttpServletResponse) : ContentCachingResponseWrapper(response) {
    /**
     * HTTP 응답 컨텐츠에 대한 문자열 프로퍼티입니다.
     *
     * 컨텐츠 타입이 human-readable한 경우, 컨텐츠의 바이트 배열을 문자열로 변형하여 반환합니다.
     */
    val contentAsString: String by lazy {
        if ((contentType?.let { MediaType.valueOf(it) } ?: MediaType.ALL).isVisible()) {
            val csn = characterEncoding ?: "ISO-8859-1"
            val charset = if (Charset.isSupported(csn)) Charset.forName(csn) else Charset.defaultCharset()

            String(contentAsByteArray, charset)
        } else {
            "*"
        }
    }
}