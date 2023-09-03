package com.example.querydsl.repository.order


import com.example.querydsl.domain.EntityAuditing
import com.example.querydsl.logger
import com.example.querydsl.repository.order.QOrder.order
import com.example.querydsl.repository.support.Querydsl4RepositorySupport
import com.example.querydsl.repository.user.QUser
import com.example.querydsl.repository.user.User
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

    @Column(name = "coupon_id", nullable = false)
    val couponId: Long?

) : EntityAuditing()

interface OrderRepository : JpaRepository<Order, Long>, OrderCustomRepository

interface OrderCustomRepository {
    fun find(pageable: Pageable): Page<Order>
    fun find2(pageable: Pageable): Page<Order>
    fun find3(
        pageable: Pageable
    ): Page<Order>
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


    override fun find3(
        pageable: Pageable
    ): Page<Order> {
        log.info("thread find3 : ${Thread.currentThread()}")
        return applyPagination(
            pageable = pageable,
            contentQuery = { selectFrom(order).where(order.userId.isNotNull) },
            countQuery = { select(order.count()).from(order).where(order.userId.isNotNull) },
        )
    }

    override fun find2(pageable: Pageable): Page<Order> {
        val query = selectFrom(order)
        return PageImpl(querydsl.applyPagination(pageable, query).fetch(), pageable, query.fetchCount())
    }
}

@RestController
@RequestMapping("/api/orders")
class OrderApi(
    private val orderRepository: OrderRepository
) {
    private val log by logger()

    @GetMapping
    fun getOrder(
        @PageableDefault pageable: Pageable
    ): Page<Order> {
        log.info("thread api : ${Thread.currentThread()}")
        return orderRepository.find3(pageable)
    }
}