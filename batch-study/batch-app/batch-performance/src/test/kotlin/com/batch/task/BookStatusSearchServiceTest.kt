package com.batch.task

import org.junit.jupiter.api.Test

internal class BookStatusSearchServiceTest{

    @Test
    internal fun asdasdasd() {
        val toList = (1..20L).toList()

        val latestBookStatus = BookStatusLatestService()
            .getLatestBookStatus(toList)

        for (bookStatus in latestBookStatus) {
            println(bookStatus)
        }
    }
}