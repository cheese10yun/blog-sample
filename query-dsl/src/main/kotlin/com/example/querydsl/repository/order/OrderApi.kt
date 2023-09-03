package com.example.querydsl.repository.order

import com.example.querydsl.logger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderApi(
    private val orderRepository: OrderRepository
) {
    private val log by logger()

    @GetMapping
    fun getOrder(
        @PageableDefault pageable: Pageable,
        @RequestParam(name = "address") address: String

    ): Page<Order> {
        log.info("thread api : ${Thread.currentThread()}")
//        return orderRepository.findPaging1(pageable)
        return orderRepository.findPaging2By(pageable, address)
    }

    @GetMapping("/slice")
    fun getOrderSlice(
        @PageableDefault pageable: Pageable,
        @RequestParam(name = "address") address: String
    ): Slice<Order> {
        log.info("thread api : ${Thread.currentThread()}")
//        return orderRepository.findSliceBy(pageable, address)
        return orderRepository.findSliceBy2(pageable, address)
    }
}