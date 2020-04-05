package com.example.api.filter

import org.slf4j.MDC
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.temporal.ChronoUnit.MILLIS as ChronoMillis

fun MediaType.isVisible(): Boolean = HttpLoggingFilter.VISIBLE_TYPES.contains(this)

/**
 * HTTP 요청/응답을 로그로 기록하기 위한 [OncePerRequestFilter] 구체 클래스입니다.
 */
class HttpLoggingFilter : OncePerRequestFilter() {
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
        MDC.put("REQUEST_ID", UUID.randomUUID().toString().substring(0, 7))

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
            // CAUTION! copyBodyToResponse 메서드를 실행하지 않을 경우 출력 스트림이 리셋되지 않아서 클라이언트가 응답을 받을 수 없게 됩니다.
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
            "    └─ Completed with ${response.status} in ${ChronoMillis.between(startedAt, LocalDateTime.now())} ms")

    private fun HttpServletRequest.wrap(): ContentReadingHttpRequestWrapper = ContentReadingHttpRequestWrapper(this)

    private fun HttpServletResponse.wrap(): ContentReadingHttpResponseWrapper = ContentReadingHttpResponseWrapper(this)

    companion object {
        internal val VISIBLE_TYPES = listOf(
            MediaType.valueOf("text/*"),
            MediaType.APPLICATION_FORM_URLENCODED,
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml"),
            MediaType.MULTIPART_FORM_DATA
        )
    }
}