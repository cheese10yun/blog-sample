package com.cheese.yun.io.slack

import com.cheese.yun.io.log
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost

class SlackClient {

    private val baseUrl = "https://hooks.slack.com/services"
    private val channelBot = "/T9QDU7RFD/B9RCFTYKY/iPnwmo76uFvn11Bsh3JvxVoJ"

    fun sendJobReport(message: String) {
        "$baseUrl$channelBot"
            .httpPost()
            .jsonBody("""
                {
                    "text": "$message"
                }
            """.trimIndent())
            .response()
            .apply { log() }
    }
}