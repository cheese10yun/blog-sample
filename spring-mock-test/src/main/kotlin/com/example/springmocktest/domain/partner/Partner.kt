package com.example.springmocktest.domain.partner

import com.example.springmocktest.domain.EntityAuditing
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "partner")
class Partner(

    @Column(name = "account_number", nullable = false)
    var accountNumber: String,

    @Column(name = "account_holder", nullable = false)
    val accountHolder: String,

    @Column(name = "name", nullable = false)
    val name: String

) : EntityAuditing() {

}