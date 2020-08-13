package com.example.querydsl.service

import com.example.querydsl.domain.EntityAuditing
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Service
class CouponService(
    private val saveService: SaveService
) {
    fun something(i: Int) {
        println("something CurrentTransactionName: ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        saveService.save(i)
    }
}

@Service
class SaveService(
    private val couponRepository: CouponRepository
) {
//    @Transactional
    fun save(i: Int) {
        println("save CurrentTransactionName: ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        (1..i).map {
            if (it == 20) {
                throw RuntimeException("$i ....")
            }
            couponRepository.save(Coupon(it.toBigDecimal()))
        }
    }
}

@Entity
@Table(name = "coupon")
data class Coupon(

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal
) : EntityAuditing()

interface CouponRepository : JpaRepository<Coupon, Long>