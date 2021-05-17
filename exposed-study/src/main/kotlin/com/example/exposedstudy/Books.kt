package com.example.exposedstudy

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object Books : LongIdTable("book") {
    val writerId = reference("writer_id", Writers)
    val title = varchar("title", 150)
    val price = decimal("price", 10, 4)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Writers : LongIdTable("writer") {
    val name = varchar("name", 150)
    val email = varchar("email", 150)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}