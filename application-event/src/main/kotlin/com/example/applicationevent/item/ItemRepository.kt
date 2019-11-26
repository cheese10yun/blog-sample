package com.example.applicationevent.item

import org.springframework.data.jpa.repository.JpaRepository

interface ItemRepository : JpaRepository<Item, Long> {

    fun findByCodeIn(code: List<String>): List<Item>
}