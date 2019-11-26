package com.example.applicationevent.cart

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface CartRepository : JpaRepository<Cart, Long> {

    @Transactional
    @Modifying
    @Query("delete from Cart c where c.code in :codes")
    fun deleteAllByCodes(@Param("codes") codes: List<String>)

}