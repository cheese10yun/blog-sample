package com.service.order

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * [Filter] 구현체를 위한 [Configuration] 빈 모음
 */
@Configuration
class FilterConfig {
//    @Bean
//    fun httpLoggingFilter(): Filter = HttpLoggingFilter()
}

/**
 * HTTP 요청/응답을 로그로 기록하기 위한 [OncePerRequestFilter] 구체 클래스입니다.
 *
 */
internal class HttpLoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (super.isAsyncDispatch(request)) {
            filterChain.doFilter(request, response)
        } else {
            doFilterWrapped(request.wrap(), response.wrap(), filterChain)
        }
    }

    /**
     * 래핑된 [HttpServletRequest]와 [HttpServletResponse]를 이용해 로그 기록을 수행합니다.
     *
     * @param request [HttpServletRequest]를 래핑한 [ContentReadingHttpRequestWrapper]
     * @param response [HttpServletResponse]를 래핑한 [ContentReadingHttpResponseWrapper]
     * @param filterChain [FilterChain]
     */
    private fun doFilterWrapped(
        request: ContentReadingHttpRequestWrapper,
        response: ContentReadingHttpResponseWrapper,
        filterChain: FilterChain
    ) {
        val (requestLog, startedAt) = beforeRequest(request)

        try {
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            when (e) {
                is IOException,
                is ServletException -> logger.error("요청/응답에 대한 필터 처리 중에 오류가 발생했습니다.", e)
                else -> throw e
            }
        } finally {
            afterResponse(response, requestLog, startedAt)
        }
    }

    /**
     * 로그 기록의 전처리 과정을 진행합니다.
     *
     * @param request [HttpServletRequest]를 래핑한 [ContentReadingHttpRequestWrapper]
     * @return HTTP 요청에 대한 로그와 요청 시작 시간의 [Pair]
     */
    private fun beforeRequest(request: ContentReadingHttpRequestWrapper): Pair<String, LocalDateTime> {
        return yieldRequestLog(request, LocalDateTime.now())
    }

    /**
     * HTTP 요청에 대한 로그를 만듭니다.
     *
     * @param request [HttpServletRequest]를 래핑한 [ContentReadingHttpRequestWrapper]
     * @param startedAt 요청 시작 일시
     */
    private fun yieldRequestLog(request: ContentReadingHttpRequestWrapper, startedAt: LocalDateTime): Pair<String, LocalDateTime> =
        Pair(
            "\n    ⊙ ${request.method} ${request.requestURI}\n" +
                "    ├─ Started At: ${startedAt}\n" +
                "    ├─ Headers: ${request.headerNames.toList().joinToString { "$it: ${request.getHeader(it)}" }}\n" +
                "    ├─ Params: ${request.parameterMap.map { it.key to (it.value.joinToString(", ", "[", "]")) }.joinToString(", ", "{", "}")}\n" +
                "    ├─ Request Body: ${request.contentAsString}\n",
            startedAt
        )

    /**
     * 로그 기록의 후처리 과정을 진행합니다.
     *
     * @param response [HttpServletResponse]를 래핑한 [ContentReadingHttpResponseWrapper]
     * @param requestLog 전처리 과정에서 만들어진 HTTP 요청에 대한 로그
     * @param startedAt 요청 시작 일시
     */
    private fun afterResponse(response: ContentReadingHttpResponseWrapper, requestLog: String, startedAt: LocalDateTime) {
        try {
            logRequestAndResponse(response, requestLog, startedAt)
        } finally {
            response.copyBodyToResponse()
        }
    }

    /**
     * 요청과 응답에 대한 로그를 기록합니다.
     *
     * @param response [HttpServletResponse]를 래핑한 [ContentReadingHttpResponseWrapper]
     * @param requestLog 전처리 과정에서 만들어진 HTTP 요청에 대한 로그
     * @param startedAt 요청 시작 일시
     */
    private fun logRequestAndResponse(response: ContentReadingHttpResponseWrapper, requestLog: String, startedAt: LocalDateTime) =
        logger.info(requestLog +
            "    ├─ Response Body: ${response.contentAsString}\n" +
            "    └─ Completed with ${response.status} in ${ChronoUnit.MILLIS.between(startedAt, LocalDateTime.now())} ms")

    private fun HttpServletRequest.wrap(): ContentReadingHttpRequestWrapper = ContentReadingHttpRequestWrapper(this)

    private fun HttpServletResponse.wrap(): ContentReadingHttpResponseWrapper = ContentReadingHttpResponseWrapper(this)

    companion object {
        internal val VISIBLE_TYPES = hashSetOf(
            MediaType.valueOf("text/*"),
            MediaType.APPLICATION_FORM_URLENCODED,
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_JSON_UTF8,
            MediaType.APPLICATION_XML,
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml"),
            MediaType.MULTIPART_FORM_DATA
        )
    }
}

/**
 * HTTP 요청 컨텐츠를 읽고 스트림을 리셋하여 다시 읽을 수 있도록 하는 [HttpServletRequest]의 래퍼
 *
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

fun MediaType.isVisible(): Boolean = HttpLoggingFilter.VISIBLE_TYPES.contains(this)