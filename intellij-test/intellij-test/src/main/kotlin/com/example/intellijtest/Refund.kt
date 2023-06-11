package com.example.intellijtest

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.util.Assert

@Entity
@Table(name = "refund")
class Refund protected constructor() : EntityAuditing() {

    @Embedded
    var account: Account? = null

    @Embedded
    var creditCard: CreditCard? = null

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null

    constructor(
        account: Account,
        order: Order
    ) : this() {
        this.account = account
        this.order = order
    }

    constructor(
        account: Account,
        creditCard: CreditCard
    ) : this() {
        this.account = account
        this.creditCard = creditCard
    }
}


@Embeddable
class Account constructor(
    bankName: String,
    accountNumber: String,
    accountHolder: String
) {
    @Column(name = "bank_name", nullable = false)
    val bankName: String

    @Column(name = "account_number", nullable = false)
    val accountNumber: String

    @Column(name = "account_holder", nullable = false)
    val accountHolder: String

    // 불안전한 객채 생성 패턴
    // 그냥 단순하게 검증 아니라, 객체의 본인의 책임을 다하는 코드로 변경 했음
    init {
        Assert.hasText(bankName, "bankName mut not be empty")
        Assert.hasText(accountNumber, "accountNumber mut not be empty") // 특수문자 제거 or "-"  제거
        Assert.hasText(accountHolder, "accountHolder mut not be empty")
        this.bankName = bankName
        this.accountNumber = accountNumber
        this.accountHolder = accountHolder
    }
}


@Embeddable
class CreditCard constructor(
    creditNumber: String,
    creditHolder: String
) {
    @Column(name = "credit_number", nullable = false)
    val creditNumber: String

    @Column(name = "credit__holder", nullable = false)
    val creditHolder: String

    init {
        Assert.hasText(creditNumber, "creditNumber must not be empty")
        Assert.hasText(creditHolder, "creditHolder must not be empty")
        this.creditNumber = creditNumber
        this.creditHolder = creditHolder
    }
}
