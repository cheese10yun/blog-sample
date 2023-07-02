# Kotlin 기반 경량 ORM Exposed 추가 정리 part 2

이전 포스팅 [Exposed: 경량 ORM](https://cheese10yun.github.io/exposed/)에서 소개한 적이 있습니다. 이번에는 실제 자주 사용하는 기능들 위주로 다루어 보겠습니다.

## Table Object

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

Table Object는 이전 포스팅 [Exposed: 경량 ORM](https://cheese10yun.github.io/exposed/)에서 소개한 적 있어서 중복되는 설명은 진행하지 않고 없는 기능에 대해서 이야기해 보겠습니다.

## clientDefault 기능

clientDefault는 클라이언트에서 default 값을 지정할 수 있는 기능입니다. created_at, updated_at과 같은 기본 생성 날짜 같은 칼럼에 사용할 때 유용합니다.

```kotlin
class ExposedTest : ExposedTestSupport() {
    
    @Test
    fun `writer insert`() {
        val connect = Database.connect(dataSource)
        transaction(connect) {
            //given
            // SQL Qury INSERT INTO writer (created_at, email, `name`, updated_at) VALUES ('2022-05-29T19:46:46.845358', 'asd@asd.com', 'name', '2022-05-29T19:46:46.845382')
            val insert = Writers.insert { writer ->
                writer[this.name] = "name"
                writer[this.email] = "asd@asd.com"
            }

            //when
            then(Writers.createdAt).isNotNull // 2022-05-29T19:46:46.845358
            then(Writers.updatedAt).isNotNull // 2022-05-29T19:46:46.845382
        }
    }
}
```

updatedAt, createdAt을 값을 insert에서 지정하지 않았지만 clientDefault를 통해서 자동으로 값을 지정되는 것을 확인할 수 있습니다.

```kotlin
class ExposedTest : ExposedTestSupport() {
    
    @Test
    fun `writer updatedAt test`() {
        val connect = Database.connect(dataSource)
        transaction(connect) {
            //given
            val insert = Writers.insert { writer ->
                writer[this.name] = "name"
                writer[this.email] = "asd@asd.com" 
            }

            //when
            val id = insert[Writers.id]
            // SQL Qury UPDATE writer SET email='new@asd.com', updated_at='2022-05-29T20:08:48.411516' WHERE writer.id = 27
            Writers.update({ Writers.id eq id })
            {
                it[this.email] = "new@asd.com"
                it[this.updatedAt] = LocalDateTime.now() // 해당 코드가 없는 경우 아래 then 실패
            }
            
            //then
            val findWriter = Writers.select(Writers.id eq id).first()
            then(insert[Writers.updatedAt]).isNotEqualTo(findWriter[Writers.updatedAt])
        }
    }
}
```

clientDefault는 생성 시에만 동작하고 업데이트에서는 동작하지 않습니다. 위 업데이트에서 `it[this.updatedAt]`를 지정하지 않는 경우에는 테스트가 실패하게 됩니다. 즉 칼럼 업데이트는 수기로 진행해야 합니다. 다음은 DAO 방식입니다. JPA로 비교했을 때는 엔티티 방식에 해당합니다.

```kotlin
class Writer(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Writer>(Writers)

    var name by Writers.name
    var email by Writers.email
    var createdAt by Writers.createdAt
    var updatedAt by Writers.updatedAt
}

class ExposedTest : ExposedTestSupport() {
    
    @Test
    fun `writer `() {
        val connect = Database.connect(dataSource)
        lateinit var id: EntityID<Long>
        lateinit var initUpdatedAt: LocalDateTime
        transaction(connect) {
            //given
            val writer = Writer.new {
                instance(
                    name = "22222",
                    email = "asd@asd"
                )
            }
            id = writer.id
            initUpdatedAt = writer.updatedAt
        }

        transaction(connect) {
            //given
            // SQL Query UPDATE writer SET email='new@sd.com', `name`='new', updated_at='2022-05-29T21:00:45.562978' WHERE id = 36
            val findWriter = Writer.findById(id)!!
            findWriter.name = "new"
            findWriter.email = "new@sd.com"
            findWriter.updatedAt = LocalDateTime.now() // 주석시 아래 검증 실패
        }

        transaction(connect) {
            //given
            val findWriter = Writer.findById(id)!!
            then(initUpdatedAt).isNotEqualTo(findWriter.updatedAt)
        }
    }
}
```
DAO 방식도 DSL 방식과 마찬가지로 업데이트를 명시하지 않으면 동작하지 않습니다.


## enumerationByName

Enum 타입에 해당하는 칼럼의 경우 enumerationByName을 사용하면 편리하게 바인딩 가능합니다. 혹시 단순 문자열이 아닌 순번 타입의 경우는 enumeration을 사용하면 됩니다.

```kotlin
object Books : LongIdTable("book") {
    val writer = reference("writer_id", Writers)
    val title = varchar("title", 150)
    val status = enumerationByName("status", 150, BookStatus::class)
    val price = decimal("price", 10, 4)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

class ExposedTest : ExposedTestSupport() {

    @Test
    fun `book insert`() {
        // SQL Log INSERT INTO book (created_at, price, status, title, updated_at, writer_id) VALUES ('2022-05-29T21:12:27.464364', 1000, 'NONE', 'test', '2022-05-29T21:12:27.464374', 37)
        val book = Books.insert { book ->
            book[this.writer] = insertWriter("asd", "asd")[Writers.id]
            book[this.title] = "test"
            book[this.price] = 1000.toBigDecimal()
            book[this.status] = BookStatus.NONE
            book[this.createdAt] = LocalDateTime.now()
            book[this.updatedAt] = LocalDateTime.now()
        }

        then(book[Books.status]).isEqualTo(BookStatus.NONE)
    }
}
```
저장 같은 경우는 Enum 객체를 그대로 사용하면 되고 가져오는 것도 동일합니다.


## 연관관계 객체 매핑 없는 경우 조인


```kotlin
fun `연관관계 객체 잠조 조인`() {
    val writerId = insertWriter("yun", "yun@asd.com")[Writers.id].value
    (1..5).map {
        insertBook("$it-title", BigDecimal.TEN, writerId)
    }

    // SELECT book.id, book.title, book.price, writer.`name`, writer.email FROM book INNER JOIN writer ON writer.id = book.writer_id
    (Books innerJoin Writers)
        .slice(
            Books.id,
            Books.title,
            Books.price,
            Writers.name,
            Writers.email,
        )
        .selectAll()
        .forEach {
            it.fieldIndex
            println("bookId: ${it[Books.id]}, title: ${it[Books.title]}, writerName: ${it[Writers.name]}, writerEmail: ${it[Writers.email]}")
        }
}
```
연관관계를 객체 기반으로 설정한 경우 위 코드처럼 어렵지 않게 조인을 진행할 수 있습니다. 하지만 객체 연관관계를 설정하지 않는 경우에는 위처럼 조인을 진행할 수 없고 아래와 같은 방법으로 진행해야 합니다.

```kotlin
object Publishers: LongIdTable("publisher") {
    val writerId = long("writer_id")
    val corpName = varchar("corp_name", 150)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
```

연관관계를 객체 기반으로 하는 것이 아니라 단순 long type으로 지정하여 테이블 객체를 선언합니다. JPA에서도 연관관계 탐색의 오용을 경계하는 것처럼 Exposed에서도 동일하게 무리한 객체 연결은 지양하는 것이 바람직하다고 생각합니다.

```kotlin
class ExposedTest : ExposedTestSupport() {
    @Test
    fun `연관관계 없는 조인`() {
        val connect = Database.connect(dataSource)
        transaction(connect) {
            val writer = Writers.insert {
                it[this.name] = "name"
                it[this.email] = "name@add.cpm"
            }
            val publisher = Publishers.insert {
                it[this.writerId] = writer[Writers.id].value
                it[this.corpName] = "corp name"
            }

            Publishers
                .join(
                    otherTable = Writers, // (1)
                    joinType = JoinType.LEFT, // (2)
                    additionalConstraint = { // (3)
                        Publishers.writerId eq Writers.id
                    }
                )
                .select {
                    Publishers.id eq publisher[Publishers.id].value
                }
                .forEach {
                    println("Publishers.id : ${it[Publishers.id]}")
                    println("Publishers.corpName : ${it[Publishers.corpName]}")
                    println("Publishers.writerId : ${it[Publishers.writerId]}")
                    println("Writers.id : ${it[Writers.id]}")
                    println("Writers.name : ${it[Writers.name]}")
                    println("Writers.email : ${it[Writers.email]}")
                }
        }
    }
}
```

* (1): 조인할 대상 객체를 지정합니다.
* (2): 조인 타입을 지정합니다.
* (3): 조인 대상의 조건으로 on 절에 해당합니다.

```sql
SELECT publisher.id,
       publisher.writer_id,
       publisher.corp_name,
       publisher.created_at,
       publisher.updated_at,
       writer.id,
       writer.`name`,
       writer.email,
       writer.created_at,
       writer.updated_at
FROM publisher
         LEFT JOIN writer ON (publisher.writer_id = writer.id)
WHERE publisher.id = 4
```

실제 원하는 방식으로 조인이 진행되는 것을 확인할 수 있습니다.


특정 조건에 따라 join을 해야 하는 경우가 있습니다. 예를 들어 특정 조건에 만족하는 경우 필요 테이블에 조인을 하여 필요 데이터를 가져오는 경우 Exposed에서는 다음과 같이 진행할 수 있습니다.

```kotlin
class ExposedTest : ExposedTestSupport() {

    @Test
    fun `연관관계 없는 조인2`() {
        val connect = Database.connect(dataSource)
        transaction(connect) {
            val writer = Writers.insert {
                it[this.name] = "name"
                it[this.email] = "name@add.cpm"
            }
            val publisher = Publishers.insert {
                it[this.writerId] = writer[Writers.id].value
                it[this.corpName] = "corp name"
            }

            val needJoin = true // (1) 

            Publishers
                .slice(
                    Publishers.id,
                    Publishers.corpName,
                    Publishers.writerId
                )
                .select {
                    Publishers.id eq publisher[Publishers.id].value
                }
                .apply {
                    // (2)
                    if (needJoin) {
                        this.adjustColumnSet {
                            join(
                                otherTable = Writers,
                                joinType = JoinType.LEFT,
                                additionalConstraint = {
                                    Publishers.writerId eq Writers.id
                                }
                            )
                        }
                        this.adjustSlice {
                            // (3)
                            slice(it.fields + Writers.id + Writers.name + Writers.email)
                        }
                    }
                }
                .forEach {
                    println("Publishers.id : ${it[Publishers.id]}")
                    println("Publishers.corpName : ${it[Publishers.corpName]}")
                    println("Publishers.writerId : ${it[Publishers.writerId]}")
                    if (needJoin) {
                        println("Writers.id : ${it[Writers.id]}")
                        println("Writers.name : ${it[Writers.name]}")
                        println("Writers.email : ${it[Writers.email]}")
                    }
                }
        }
    }
}

```
* (1): 특정 조건에 따라 조인 여부를 결정하는 분기 값
* (2): 조건에 만족하는 경우 조인을 진행
* (3): 조인을 진행한 경우 추가 적으로 필요한 칼럼을 추가


```sql
# needJoin = false 경우
SELECT publisher.id,
       publisher.corp_name,
       publisher.writer_id
FROM publisher
WHERE publisher.id = 7;

# needJoin = true 경우
SELECT publisher.id,
       publisher.corp_name,
       publisher.writer_id,
       writer.id,
       writer.`name`,
       writer.email
FROM publisher
         LEFT JOIN writer ON (publisher.writer_id = writer.id)
WHERE publisher.id = 8
```
needJoin 분기에 따라 쿼리문이 달라지는 것을 확인할 수 있습니다.

## 참고
* [Exposed Wiki](https://github.com/JetBrains/Exposed/wiki)