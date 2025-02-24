package com.example.mongostudy.order

import com.example.mongostudy.mongo.Auditable
import java.math.BigDecimal
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@Document(collection = "order_item")
class OrderItem(
    @Field(name = "items", targetType = FieldType.ARRAY)
    val items: List<Item> = emptyList()
) : Auditable() {

    override fun toString(): String {
        return "OrderItem(items=$items)"
    }
}

data class Item(
    @Field(name = "name", targetType = FieldType.STRING)
    val name: String,

    @Field(name = "category", targetType = FieldType.STRING)
    val category: String,

//    @Field(name = "price")
    @Field(name = "price", targetType = FieldType.DECIMAL128)
    val price: BigDecimal,
)


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