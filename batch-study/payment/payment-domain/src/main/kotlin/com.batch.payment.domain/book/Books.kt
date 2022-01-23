package com.batch.payment.domain.book

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime

object Books : LongIdTable(name = "book") {
    val status = enumerationByName("status", 20, BookStatus::class)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}