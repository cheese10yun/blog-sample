package com.cheese.yun.domain.order

import com.cheese.yun.domain.model.Address
import com.cheese.yun.domain.support.EntityAuditing
import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "orders")
class Order(

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "zipCode", column = Column(name = "zip_code", nullable = false)),
        AttributeOverride(name = "city", column = Column(name = "city", nullable = false)),
        AttributeOverride(name = "detailAddress", column = Column(name = "detail_address", nullable = false))
    )
    val address: Address,

    @Column(name = "price", nullable = false)
    val price: BigDecimal
) : EntityAuditing() {


}