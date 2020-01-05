package com.dataflow.sample.domain

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "bill")
data class Bill(
        @Column(name = "first_name", nullable = false)
        var firstName: String,

        @Column(name = "last_name", nullable = false)
        val lastName: String,

        @Column(name = "data_usage", nullable = false)
        val dataUsage: Long,

        @Column(name = "minutes", nullable = false)
        val minutes: Long,

        @Column(name = "bill_amount", nullable = false)
        val billAmount: BigDecimal
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    fun changeFirstName() {
        this.firstName = "reset"
    }
}

