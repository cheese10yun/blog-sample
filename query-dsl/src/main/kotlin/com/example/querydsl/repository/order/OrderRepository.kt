package com.example.querydsl.repository.order


import com.example.querydsl.domain.EntityAuditing
import com.example.querydsl.logger
import com.example.querydsl.repository.order.QOrder.order
import com.example.querydsl.repository.support.Querydsl4RepositorySupport
import com.querydsl.jpa.impl.JPAQuery
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
    fun findPagingBy(pageable: Pageable, address: String): Page<Order>
    fun findPaging1(pageable: Pageable, address: String): Page<Order>

    fun findSliceBy(pageable: Pageable, address: String): Slice<Order>
    fun findPaging2By(pageable: Pageable, address: String): Page<Order>
    fun findSliceBy2(pageable: Pageable, address: String): Slice<Order>
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

    override fun findPagingBy(pageable: Pageable, address: String): Page<Order> {
        val query = selectFrom(order).where(order.address.eq(address))
        return PageImpl(querydsl.applyPagination(pageable, query).fetch(), pageable, query.fetchCount())
    }

    override fun findPaging2By(pageable: Pageable, address: String): Page<Order> {
        val query: JPAQuery<Order> = from(order).select(order).where(order.address.eq(address))
        val content: List<Order> = querydsl.applyPagination(pageable, query).fetch()
        val totalCount: Long = query.fetchCount()
        return PageImpl(content, pageable, totalCount)
    }

    override fun findSliceBy(pageable: Pageable, address: String): Slice<Order> {
        val query: JPAQuery<Order> = from(order).select(order).where(order.address.eq(address))
        val content: List<Order> = querydsl.applyPagination(pageable, query).fetch().toList().filterNotNull()
        val hasNext: Boolean = content.size >= pageable.pageSize
        return SliceImpl(content, pageable, hasNext)
    }

    override fun findSliceBy2(pageable: Pageable, address: String): Slice<Order> {
        return applySlicePagination(
            pageable = pageable,
            query = {
                selectFrom(order).where(order.address.eq(address))
            }
        )
    }
}