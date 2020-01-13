package com.datajdbc.sample

import java.math.BigDecimal

data class Book(
        var id: Long? = null,
        var name: String,
        var price: BigDecimal
) {


}