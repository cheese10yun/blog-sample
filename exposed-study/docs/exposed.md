# Exposed

* [ ] 다이나믹 조인
* [ ] enum type
* [ ] 컨버터
* [ ] 신규 기능

## Enum

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