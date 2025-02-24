package com.example.boot3mongo.order

import java.math.BigDecimal
import org.bson.types.ObjectId

object OrderItemQueryForm {
    data class UpdateItem(
        val orderItem: ObjectId,
        val items: List<UpdateItemForm>

    )

    data class UpdateItemForm(
        val name: String,
        val category: String,
        val price: BigDecimal
    )
}