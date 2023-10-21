package com.example.mongostudy

//import com.example.mongostudy.coupon.Coupon
//import com.example.mongostudy.coupon.CouponRepository
//import com.example.mongostudy.coupon.CouponStatus
//import com.example.mongostudy.member.Member
//import com.example.mongostudy.member.MemberRepository
//import com.example.mongostudy.member.MembershipStatus
//import com.example.mongostudy.order.Order
//import com.example.mongostudy.order.OrderRepository
//import com.example.mongostudy.order.OrderStatus
//import kotlin.random.Random
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }

@SpringBootApplication
class MongoStudyApplication

fun main(args: Array<String>) {
    runApplication<MongoStudyApplication>(*args)
}



//@Component
//class MnogoDataSetUp(
//    private val orderRepository: OrderRepository,
//    private val memberRepository: MemberRepository,
//    private val couponRepository: CouponRepository
//) : ApplicationRunner {
//
//    private val random = Random
//
//    override fun run(args: ApplicationArguments) {
//
//        val members = (1..50).map {
//            Member(
//                memberId = UUID.randomUUID().toString(),
//                memberName = "Member $it",
//                email = "member$it@example.com",
//                dateJoined = LocalDateTime.now(),
//                dateOfBirth = LocalDate.of(1990 + random.nextInt(20), 1 + random.nextInt(11), 1 + random.nextInt(28)),
//                phoneNumber = "123-456-$it",
//                address = "Address $it",
//                membershipStatus = MembershipStatus.values()[random.nextInt(MembershipStatus.values().size)],
//                pointsAccumulated = BigDecimal(random.nextInt(1000)),
//                lastPurchaseDate = LocalDateTime.now().minusDays(random.nextLong(30))
//            )
//        }
//        memberRepository.saveAll(members)
//
//        val coupons = (1..50).map {
//            Coupon(
//                couponId = "COUPON$it",
//                couponName = "Coupon $it",
//                discountAmount = BigDecimal(random.nextInt(100)),
//                expiryDate = LocalDate.now().plusDays(random.nextLong(100)),
//                status = CouponStatus.values()[random.nextInt(CouponStatus.values().size)],
//                memberId = members[random.nextInt(members.size)].memberId,
//                issuedDate = LocalDateTime.now().minusDays(random.nextLong(30)),
//                usedDate = LocalDateTime.now(),
//                couponType = "TYPE${random.nextInt(5)}",
//                minimumOrderValue = BigDecimal(50 + random.nextInt(50))
//            )
//        }
//        couponRepository.saveAll(coupons)
//
//        val orders = (1..50).map {
//            Order(
//                orderId = "ORDER$it",
//                orderDate = LocalDateTime.now().minusHours(random.nextLong(720)),
//                productName = "Product $it",
//                productPrice = BigDecimal(10 + random.nextInt(90)),
//                shippingAddress = "Shipping Address $it",
//                orderStatus = OrderStatus.values()[random.nextInt(OrderStatus.values().size)],
//                paymentMethod = "METHOD${random.nextInt(5)}",
//                memberId = members[random.nextInt(members.size)].memberId,
//                deliveryDate = LocalDate.now().plusDays(random.nextLong(10)),
//                quantity = random.nextInt(5) + 1
//            )
//        }
//        orderRepository.saveAll(orders)
//    }
//}
