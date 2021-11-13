package com.service.order

import kotlin.math.log
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
//import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.openfeign.EnableFeignClients
//import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableFeignClients
@RefreshScope
class OrderApplication

fun main(args: Array<String>) {
    runApplication<OrderApplication>(*args)
}

@Component
class OrderApplicationRunner(
    private val orderRepository: OrderRepository
) : ApplicationRunner {
    private val log by logger()

    override fun run(args: ApplicationArguments) {
        orderRepository.saveAllAndFlush(
            listOf(
                Order(
                    productId = "5566da6f-3f03-4ce5-8863-3c142e452522",
                    userId = "5566da6f-3f03-4ce5-8863-3c142e452522",
                    orderId = "5566da6f-3f03-4ce5-8863-3c142e452522",
                    qty = 3,
                    unitPrice = 100,
                    totalPrice = 300
                ),
                Order(
                    productId = "997a5a8b-80e4-4a5d-b5d1-14ee22be18da",
                    userId = "997a5a8b-80e4-4a5d-b5d1-14ee22be18da",
                    orderId = "997a5a8b-80e4-4a5d-b5d1-14ee22be18da",
                    qty = 3,
                    unitPrice = 100,
                    totalPrice = 300
                )
            )
        )
    }

    // https://www.tabnine.com/code/java/classes/org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent
    // https://www.alibabacloud.com/blog/knowledge-sharing---introduction-to-the-spring-cloud-bus-message-bus_594823
    // https://www.programmersought.com/article/86735377064/
//    @EventListener
//    fun onRefreshRemoteEvent(event: RefreshRemoteApplicationEvent) {
//        log.info("Event....")
//        log.info(event.id)
//        log.info(event.source.toString())
//        log.info(event.originService)
//        log.info(event.destinationService)
//        log.info("Event....")
//    }
}

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }