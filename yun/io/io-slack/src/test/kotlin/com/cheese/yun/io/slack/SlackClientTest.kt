package com.cheese.yun.io.slack

import org.junit.jupiter.api.Test

internal class SlackClientTest{

    @Test
    internal fun name() {
        SlackClient()
            .sendJobReport("""I am a test message""")
    }
}