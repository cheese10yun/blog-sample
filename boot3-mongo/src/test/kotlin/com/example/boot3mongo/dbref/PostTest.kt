package com.example.boot3mongo.dbref

import com.example.boot3mongo.Boot3MongoApplicationTest
import com.example.boot3mongo.order.OrderItemRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PostTest(
    private val postRepository: PostRepository
) : Boot3MongoApplicationTest() {

    @Test
    fun `updateItems`() {
        (1..5).map {
            Post(
                title = "title-$it",
                content = "content-$it",
                author = mongoTemplate.save(Author(name = "Sondra Snow-$it"))
            )
        }
            .let {
                mongoTemplate.insertAll(it)
            }

        println("postRepository.findAll() = ${postRepository.findAll()}")
    }
}