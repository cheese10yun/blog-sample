package com.example.mongostudy

import com.example.mongostudy.coupon.Coupon
import com.example.mongostudy.coupon.CouponRepository
import com.example.mongostudy.coupon.CouponStatus
import com.example.mongostudy.member.Address
import com.example.mongostudy.member.Member
import com.example.mongostudy.member.MemberRepository
import com.example.mongostudy.member.MemberStatus
import com.example.mongostudy.order.Order
import com.example.mongostudy.order.OrderRepository
import com.example.mongostudy.order.OrderStatus
import kotlin.random.Random
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Component
class MnogoDataSetUp(
    private val orderRepository: OrderRepository,
    private val memberRepository: MemberRepository,
    private val couponRepository: CouponRepository
) : ApplicationRunner {

    private val random = Random

    override fun run(args: ApplicationArguments) {

        val members = (1..50).map {
            Member(
                memberId = UUID.randomUUID().toString(),
                name = "Member $it",
                email = "member$it@example.com",
                dateJoined = LocalDateTime.now(),
                dateOfBirth = LocalDate.of(1990 + Random.nextInt(20), 1 + Random.nextInt(11), 1 + Random.nextInt(28)),
                phoneNumber = "123-456-$it",
                address = Address(
                    address = "address $it",
                    addressDetail = "address detail $it",
                    zipCode = "zip code - $it"
                ),
                status = MemberStatus.values()[Random.nextInt(MemberStatus.values().size)],
                pointsAccumulated = BigDecimal(Random.nextInt(1000)),
                lastPurchaseDate = LocalDateTime.now().minusDays(Random.nextLong(30))
            )
        }
//        memberRepository.saveAll(members)

        val coupons = (1..50).map {
            Coupon(
                couponId = "COUPON$it",
                couponName = "Coupon $it",
                discountAmount = BigDecimal(Random.nextInt(100)),
                expiryDate = LocalDate.now().plusDays(Random.nextLong(100)),
                status = CouponStatus.values()[Random.nextInt(CouponStatus.values().size)],
                memberId = members[Random.nextInt(members.size)].memberId,
                issuedDate = LocalDateTime.now().minusDays(Random.nextLong(30)),
                usedDate = LocalDateTime.now(),
                couponType = "TYPE${Random.nextInt(5)}",
                minimumOrderValue = BigDecimal(50 + Random.nextInt(50))
            )
        }
//        couponRepository.saveAll(coupons)

        val orders = (1..50).map {
            Order(
                orderId = "ORDER$it",
                orderDate = LocalDateTime.now().minusHours(Random.nextLong(720)),
                productName = "Product $it",
                productPrice = BigDecimal(10 + Random.nextInt(90)),
                shippingAddress = "Shipping Address $it",
                orderStatus = OrderStatus.values()[Random.nextInt(OrderStatus.values().size)],
                paymentMethod = "METHOD${Random.nextInt(5)}",
                memberId = members[Random.nextInt(members.size)].memberId,
                deliveryDate = LocalDate.now().plusDays(Random.nextLong(10)),
                quantity = Random.nextInt(5) + 1
            )
        }
//        orderRepository.saveAll(orders)
    }
}