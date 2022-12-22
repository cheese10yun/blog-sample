package com.example.stock.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.LockModeType
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "stock")
class Stock constructor(
    productId: Long,
    quantity: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(name = "product_id", nullable = false)
    var productId: Long
        internal set

    @Column(name = "quantity", nullable = false)
    var quantity: Long
        internal set

    @Version
    @Column(name = "version", nullable = false)
    var version: Int
        internal set


    fun decrease(quantity: Long) {
        if (this.quantity - quantity < 0) {
            throw IllegalArgumentException("")
        }

        this.quantity = this.quantity - quantity
    }

    init {
        this.productId = productId
        this.quantity = quantity
        this.version = 0
    }
}

interface StockRepository : JpaRepository<Stock, Long> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock  s where s.id = :id")
    fun findByIdWithPessimisticLock(id: Long): Stock

    @Lock(value = LockModeType.OPTIMISTIC)
    @Query("select s from Stock  s where s.id = :id")
    fun findByIdWithOptimisticLock(id: Long): Stock
}

@Service
class StockService(
    private val stockRepository: StockRepository,
    private val entityManager: EntityManager,
) {

    //    @Transactional(isolation = Isolation.REPEATABLE_READ)
//    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    @Synchronized
    fun decrease(stockId: Long, quantity: Long) {
        val stock = stockRepository.findByIdOrNull(stockId)!!
        stock.decrease(quantity)
        stockRepository.saveAndFlush(stock)
    }
}

@Service
class PessimisticLockStockService(
    private val stockRepository: StockRepository,
) {

    @Transactional
    fun decrease(stockId: Long, quantity: Long) {
        val stock = stockRepository.findByIdWithPessimisticLock(stockId)
        stock.decrease(quantity)
        stockRepository.saveAndFlush(stock)
    }
}


@Service
class OptimisticLockStockService(
    private val stockRepository: StockRepository,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun decrease(stockId: Long, quantity: Long) {
        val stock = stockRepository.findByIdWithOptimisticLock(stockId)
        stock.decrease(quantity)
        stockRepository.saveAndFlush(stock)
    }
}

@Service
class OptimisticLockStockFaceService(
    private val optimisticLockStockService: OptimisticLockStockService,
) {

    @Transactional
    fun decrease(stockId: Long, quantity: Long) {
        while (true) {
            try {
                optimisticLockStockService.decrease(
                    stockId = stockId,
                    quantity = quantity
                )
                break
            } catch (e: Exception) {
                Thread.sleep(50)
            }
        }
    }
}

//interface LockRepository : JpaRepository<Stock, Long> {
//
//    @Query(name = "select get_lock(:key, 3000)", nativeQuery = true)
//    fun getLock(key: String): Int
////
////    @Query(name = "select release_lock(:key)", nativeQuery = true)
////    fun releaseLock(key: String): Int
//}

interface LockRepository : JpaRepository<Stock?, Long?> {
    @Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
    fun getLock(key: String): Int

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    fun releaseLock(key: String): Int
}


@Service
class NamedLockStockFacadeService(
    private val lockRepository: LockRepository,
    private val stockService: StockService,
) {

    @Transactional
    fun decrease(stockId: Long, quantity: Long) {
        try {
            val lock = lockRepository.getLock(stockId.toString())
            println("==========")
            println("lock: $lock")
            println("==========")
            stockService.decrease(
                stockId = stockId,
                quantity = quantity
            )
        } finally {
            val release = lockRepository.releaseLock(stockId.toString())
            println("==========")
            println("release: $release")
            println("==========")
        }
    }
}