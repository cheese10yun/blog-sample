package com.example.querydsl.repository.order


import com.example.querydsl.domain.EntityAuditing
import com.example.querydsl.repository.order.QOrder.order
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
}

class OrderCustomRepositoryImpl : QuerydslRepositorySupport(Order::class.java), OrderCustomRepository {

    override fun find(
        pageable: Pageable
    ) = runBlocking {
        val contentQuery = from(order).select(order).where(order.userId.isNotNull)
        val countQuery = from(order).select(order.count()).where(order.userId.isNotNull)
        val content = async { contentQuery.fetch() }
        val count = async { countQuery.fetchFirst() }

        PageImpl(content.await(), pageable, count.await())
    }

     override fun find2(pageable: Pageable): Page<Order> {
        val query = from(order)
            .select(order)

        return PageImpl(querydsl!!.applyPagination(pageable, query).fetch(), pageable, query.fetchCount())
    }
}

@RestController
@RequestMapping("/api/orders")
class OrderApi(
    private val orderRepository: OrderRepository
) {

    @GetMapping
    fun getOrder(
        @PageableDefault pageable: Pageable
    ): Page<Order> {
        return orderRepository.find(pageable)
    }
}