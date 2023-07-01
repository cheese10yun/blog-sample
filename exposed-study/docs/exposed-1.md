# Kotlin 기반 경량 ORM Exposed 추가 정리 part 2


데이터 저장소에 값을 저장하는 경우, 저장된 데이터를 가져오는 경우 적절하게 컨버팅이 필요한 경우 JPA에서는 `@Converter`를 사용하면 손쉽게 제어할 수 있습니다. Exposed에서는 `VarCharColumnType`를 확장하는 방식으로 해당 기능을 사용할 수 있습니다.



## Books DSL

```kotlin
object Books : LongIdTable("book") {
    val writer = reference("writer_id", Writers)
    val title = varchar("title", 150)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
```

