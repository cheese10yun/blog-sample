//package com.example.jpanplus1.alw.soruce
//
//import javax.persistence.*
//
//
//@Entity
//@Table(name = "fee_statistic")
//data class FeeStatistic(
//        @Id
//        @GeneratedValue(strategy = GenerationType.IDENTITY)
//        var id: Long? = null,
//
//        @Embedded
//        @AttributeOverrides(
//                AttributeOverride(name = "targetAmount.value", column = Column(name = "target_amount", nullable = true, precision = 19, scale = 10)),
//                AttributeOverride(name = "feeFactor.value", column = Column(name = "fee_factor", nullable = true, precision = 19, scale = 10))
//        )
//        val amount: FeeAmount
//) {
//
//}