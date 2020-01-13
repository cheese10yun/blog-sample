package com.datajdbc.sample

import java.math.BigDecimal
import java.util.*


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