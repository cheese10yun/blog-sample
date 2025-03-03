package com.example.mongostudy.dbref

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/posts")
class PostController(
    private val aggregationService: AggregationService,
    private val mongoTemplate: MongoTemplate,
) {

    @GetMapping
    fun getPosts(): List<Post> {
        return aggregationService.get()
    }

    @GetMapping("/insert")
    fun insert() {
        (1..1000).map {
            Post(
                title = "title-$it",
                content = "content-$it",
                author = mongoTemplate.save(Author(name = "Sondra Snow-$it"))
            )
        }
            .let {
                mongoTemplate.insertAll(it)
            }
    }
}

@Service
class AggregationService(
    private val postRepository: PostRepository,
) {

    fun get(): MutableList<Post> {
        return postRepository.findAll()
    }
}