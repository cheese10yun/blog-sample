package com.example.querydsl.repository.order


import com.example.querydsl.domain.EntityAuditing
import com.example.querydsl.logger
import com.example.querydsl.repository.order.QOrder.order
import com.example.querydsl.repository.support.Querydsl4RepositorySupport
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table


@Entity
@Table(name = "orders")
class Order(
    @Column(name = "order_number", nullable = false)
    val orderNumber: String,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "address", nullable = false)
    val address: String,

    @Column(name = "coupon_id", nullable = true)
    val couponId: Long?

) : EntityAuditing()

interface OrderRepository : JpaRepository<Order, Long>, OrderCustomRepository

interface OrderCustomRepository {
    fun find(pageable: Pageable): Page<Order>
    fun findPaging(pageable: Pageable, address: String): Page<Order>
    fun findPaging1(pageable: Pageable, address: String): Page<Order>

    fun findSlice(pageable: Pageable, address: String): Slice<Order>
    fun findPaging2(pageable: Pageable, address: String): Page<Order>
}

class OrderCustomRepositoryImpl : Querydsl4RepositorySupport(Order::class.java), OrderCustomRepository {
    private val log by logger()

    override fun find(
        pageable: Pageable
    ) = runBlocking {
        val contentQuery = selectFrom(order).where(order.userId.isNotNull)
        val countQuery = select(order.count()).from(order).where(order.userId.isNotNull)
        val content = async { contentQuery.fetch() }
        val count = async { countQuery.fetchFirst() }

        PageImpl(content.await(), pageable, count.await())
    }


    override fun findPaging1(
        pageable: Pageable,
        address: String
    ): Page<Order> {
        log.info("thread find3 : ${Thread.currentThread()}")
        return applyPagination(
            pageable = pageable,
            contentQuery = { selectFrom(order).where(order.userId.isNotNull) },
            countQuery = { select(order.count()).from(order).where(order.userId.isNotNull) },
        )
    }

    override fun findPaging(pageable: Pageable, address: String): Page<Order> {
        val query = selectFrom(order)
        return PageImpl(querydsl.applyPagination(pageable, query).fetch(), pageable, query.fetchCount())
    }

    override fun findPaging2(pageable: Pageable, address: String): Page<Order> {
        val query = from(order).select(order).where(order.address.eq(address))
        return PageImpl(querydsl.applyPagination(pageable, query).fetch(), pageable, query.fetchCount())
    }

    override fun findSlice(pageable: Pageable, address: String): Slice<Order> {
        val query = from(order).select(order)
        val fetch = querydsl.applyPagination(pageable, query).fetch().toList().filterNotNull()

        val sliceImpl = SliceImpl(fetch, pageable, fetch.isNotEmpty())
        return sliceImpl
    }

//    private fun SliceImpl(fetch: List<Order>, pageable: Pageable): Slice<Order> {
//        TODO("Not yet implemented")
//    }
}