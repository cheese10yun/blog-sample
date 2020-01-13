> [Spring Boot JDBC Examples](https://mkyong.com/spring-boot/spring-boot-jdbc-examples/) 을 보고 정리한 글입니다.


# Spring Data JDBC

## Dependency
```gradle
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    runtimeOnly("com.h2database:h2")
}
```
data-jdbc 디펜던시와, 테스트를 위한 H2 메모리 디비를 추가합니다.

## Entity
```kotlin
data class Book(
        var id: Long? = null,
        var name: String,
        var price: BigDecimal
)
```
Book Entity 객체를 만듭니다.

## Repository
```kotlin
interface BookRepository {

    fun count(): Int

    fun save(book: Book): Int

    fun update(book: Book): Int

    fun deleteById(id: Long): Int

    fun findAll(): List<Book>

    fun findByNameAndPrice(name: String, price: BigDecimal): List<Book>

    fun findById(id: Long): Optional<Book>

    fun getNameById(id: Long): String
}

@Repository
class JdbcBookRepository(
        private val jdbcTemplate: JdbcTemplate
) : BookRepository {

    override fun count(): Int {
        return jdbcTemplate
                .queryForObject("select count(*) from books", Int::class.java)!!
    }

    override fun save(book: Book): Int {
        return jdbcTemplate.update(
                "insert into books (name, price) values(?,?)",
                book.name, book.price)
    }

    override fun update(book: Book): Int {
        return jdbcTemplate.update(
                "update books set price = ? where id = ?",
                book.price, book.id)
    }

    override fun deleteById(id: Long): Int {
        return jdbcTemplate.update(
                "delete books where id = ?",
                id)
    }

    override fun findAll(): List<Book> {
        return jdbcTemplate.query(
                "select * from books"
        ) { rs: ResultSet, rowNum: Int ->
            Book(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getBigDecimal("price")
            )
        }
    }
}
```
Repository와 세부 구현체인 JdbcRepository를 작성합니다.

## Test

```kotlin

@SpringBootTest
internal class SampleApplicationKtTest {
    ....

    @Test
    internal fun name() {

        jdbcTemplate.execute("DROP TABLE books IF EXISTS")
        jdbcTemplate.execute("CREATE TABLE books(" +
                "id SERIAL, name VARCHAR(255), price NUMERIC(15, 2))")

        val books: List<Book> = listOf(
                Book(name = "Thinking in Java", price = BigDecimal("46.32")),
                Book(name = "Mkyong in Java", price = BigDecimal("1.99")),
                Book(name = "Getting Clojure", price = BigDecimal("37.3")),
                Book(name = "Head First Android Development", price = BigDecimal("41.19"))
        )

        books.forEach(Consumer { book: Book ->
            log.info("Saving...{$book.name}")
            bookRepository.save(book)
        })
        
        // count
        val count = bookRepository.count()
        log.info("[COUNT] Total books: {$count}")

        // find all
        val findAll1 = bookRepository.findAll()
        log.info("[FIND_ALL] {$findAll1}")

        // find by id
        log.info("[FIND_BY_ID] :2L")
        val book: Book = bookRepository.findById(2L).orElseThrow({ IllegalArgumentException() })
        log.info("{$book}")

        // find by name (like) and price
        log.info("[FIND_BY_NAME_AND_PRICE] : like '%Java%' and price <= 10")
        val findByNameAndPrice = bookRepository.findByNameAndPrice("Java", BigDecimal(10))
        log.info("{$findByNameAndPrice}")

        // get name (string) by id
        val nameById = bookRepository.getNameById(1L)
        log.info("[GET_NAME_BY_ID] :1L = {$nameById}")

        // update
        log.info("[UPDATE] :2L :99.99")
        book.price = BigDecimal("99.99")
        val update = bookRepository.update(book)
        log.info("rows affected: {$update}")

        // delete
        log.info("[DELETE] :3L")
        val deleteById = bookRepository.deleteById(3L)
        log.info("rows affected: {$deleteById}")

        // find all
        val findAll = bookRepository.findAll()
        log.info("[FIND_ALL] {$findAll}")
    }
}
```

```kotlin
Saving...{Book(id=null, name=Thinking in Java, price=46.32).name}
Saving...{Book(id=null, name=Mkyong in Java, price=1.99).name}
Saving...{Book(id=null, name=Getting Clojure, price=37.3).name}
Saving...{Book(id=null, name=Head First Android Development, price=41.19).name}
[COUNT] Total books: {4}
[FIND_ALL] {[Book(id=1, name=Thinking in Java, price=46.32), Book(id=2, name=Mkyong in Java, price=1.99), Book(id=3, name=Getting Clojure, price=37.30), Book(id=4, name=Head First Android Development, price=41.19)]}
[FIND_BY_ID] :2L
{Book(id=2, name=Mkyong in Java, price=1.99)}
[FIND_BY_NAME_AND_PRICE] : like '%Java%' and price <= 10
{[Book(id=2, name=Mkyong in Java, price=1.99)]}
[GET_NAME_BY_ID] :1L = {Thinking in Java}
[UPDATE] :2L :99.99
rows affected: {1}
[DELETE] :3L
rows affected: {1}
[FIND_ALL] {[Book(id=1, name=Thinking in Java, price=46.32), Book(id=2, name=Mkyong in Java, price=99.99), Book(id=4, name=Head First Android Development, price=41.19)]}
```

정상적으로 출력되는것을 확인할 수 있다.