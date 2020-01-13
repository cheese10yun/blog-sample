package com.datajdbc.sample

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.ResultSet
import java.util.*

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

    // jdbcTemplate.queryForObject, populates a single object
    override fun findById(id: Long): Optional<Book> {
        return jdbcTemplate.queryForObject(
                "select * from books where id = ?", arrayOf<Any>(id)
        ) { rs: ResultSet, rowNum: Int ->
            Optional.of(Book(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getBigDecimal("price")
            ))
        }!!
    }

    override fun findByNameAndPrice(name: String, price: BigDecimal): List<Book> {
        return jdbcTemplate.query(
                "select * from books where name like ? and price <= ?", arrayOf("%$name%", price)
        ) { rs: ResultSet, rowNum: Int ->
            Book(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getBigDecimal("price")
            )
        }
    }

    override fun getNameById(id: Long): String {
        return jdbcTemplate.queryForObject(
                "select name from books where id = ?", arrayOf<Any>(id),
                String::class.java
        )
    }
}