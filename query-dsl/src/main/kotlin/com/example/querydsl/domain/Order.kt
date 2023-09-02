//package com.example.querydsl.domain
//
//import java.math.BigDecimal
//import javax.persistence.*
//
//@Entity
//@Table(name = "orders")
//data class Order(
//    @Column(name = "amount", nullable = false)
//    var amount: BigDecimal,
//
//    @Embedded
//    var orderer: Orderer
//
//) : EntityAuditing()
//
//@Embeddable
//data class Orderer(
//    @Column(name = "member_id", nullable = false, updatable = false)
//    var memberId: Long,
//
//    @Column(name = "email", nullable = false, updatable = false)
//    var email: String
//)