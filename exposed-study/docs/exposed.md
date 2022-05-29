# Exposed


* [ ] 연관 관계
* [ ] enum type
* [ ] 다이나믹 조인
* [ ] 컨버터
* [ ] 신규 기능

## Table

```kotlin
object Writers : LongIdTable("writer") {
    val name = varchar("name", 150)
    val email = varchar("email", 150)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Books : LongIdTable("book") {
    val writer = reference("writer_id", Writers)
    val title = varchar("title", 150)
    val status = enumerationByName("status", 150, BookStatus::class)
    val price = decimal("price", 10, 4)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
```







```kotlin
object Books : LongIdTable("book") {
    val title = varchar("title", 150)
    val status = enumerationByName("status", 150, BookStatus::class)
    val price = decimal("price", 10, 4)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
```


```kotlin

    @Transactional
    fun batch(ids: List<Int>) {
        Books.batchInsert(
                data = ids,
                ignore = false,
                shouldReturnGeneratedValues = false
        ) {
            val insertWriter = insertWriter("asd", "asd")
            this[Books.writer] = insertWriter[Writers.id]
            this[Books.title] = "$it-title"
            this[Books.status] = BookStatus.NONE
            this[Books.price] = it.toBigDecimal()
            this[Books.createdAt] = LocalDateTime.now()
            this[Books.updatedAt] = LocalDateTime.now()
        }
    }
```