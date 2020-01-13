package com.datajdbc.sample

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.util.function.Consumer
import java.util.logging.Logger


@SpringBootTest
internal class SampleApplicationKtTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var bookRepository: BookRepository

    val log = Logger.getLogger(SampleApplicationKtTest::class.java.name)


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