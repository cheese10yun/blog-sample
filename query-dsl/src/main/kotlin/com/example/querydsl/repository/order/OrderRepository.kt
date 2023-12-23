package com.example.querydsl.repository.order


import com.example.querydsl.domain.EntityAuditing
import com.example.querydsl.logger
import com.example.querydsl.repository.order.QOrder.order
import com.example.querydsl.repository.support.Querydsl4RepositorySupport
import com.example.querydsl.repository.support.QuerydslCustomRepositorySupport
import com.example.querydsl.repository.user.QUser.user
import com.example.querydsl.service.QCoupon.coupon
import com.querydsl.jpa.impl.JPAQuery
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.jpa.repository.JpaRepository
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table


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
    fun findPaging3By(pageable: Pageable, address: String): Page<Order>
}

class OrderCustomRepositoryImpl : QuerydslCustomRepositorySupport(Order::class.java), OrderCustomRepository {
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
        return applyPagination(
            pageable = pageable,
            contentQuery = { selectFrom(order).where(order.userId.isNotNull) },
            countQuery = { select(order.count()).from(order).where(order.userId.isNotNull) },
        )
    }

    override fun findPagingBy(pageable: Pageable, address: String): Page<Order> {
        val query = from(order)
            .select(order)
            .innerJoin(user).on(order.userId.eq(user.id))
            .leftJoin(coupon).on(order.couponId.eq(coupon.id))
            .where(order.address.eq(address))

        return PageImpl(querydsl!!.applyPagination(pageable, query).fetch(), pageable, query.fetchCount())
    }

    override fun findPaging2By(pageable: Pageable, address: String): Page<Order> {
        val content: List<Order> = from(order)
            .select(order)
            .innerJoin(user).on(order.userId.eq(user.id))
            .leftJoin(coupon).on(order.couponId.eq(coupon.id))
            .where(order.address.eq(address))
            .run {
                querydsl!!.applyPagination(pageable, this).fetch()
            }
        val totalCount: Long = from(order)
            .select(order.count())
            .where(order.address.eq(address))
            .fetchFirst()

        return PageImpl(content, pageable, totalCount)
    }

    override fun findPaging3By(pageable: Pageable, address: String): Page<Order> = runBlocking {
        log.info("findPagingBy thread : ${Thread.currentThread()}")
        val content: Deferred<List<Order>> = async {
            log.info("content thread : ${Thread.currentThread()}")
            delay(1_000)
            from(order)
                .select(order)
                .innerJoin(user).on(order.userId.eq(user.id))
                .leftJoin(coupon).on(order.couponId.eq(coupon.id))
                .where(order.address.eq(address))
                .run {
                    querydsl!!.applyPagination(pageable, this).fetch()
                }
        }
        val totalCount: Deferred<Long> = async {
            log.info("count thread : ${Thread.currentThread()}")
            delay(500)
            from(order)
                .select(order.count())
                .where(order.address.eq(address))
                .fetchFirst()
        }

        PageImpl(content.await(), pageable, totalCount.await())
    }


    override fun findSliceBy(pageable: Pageable, address: String): Slice<Order> {
        val query: JPAQuery<Order> = from(order).select(order).where(order.address.eq(address))
        val content: List<Order> = querydsl!!.applyPagination(pageable, query).fetch()
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