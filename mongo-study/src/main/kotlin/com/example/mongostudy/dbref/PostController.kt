package com.example.mongostudy.dbref

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/posts")
class PostController(
    private val postRepository: PostRepository,
    private val mongoTemplate: MongoTemplate,
) {

    @GetMapping
    fun getPosts(): List<Post> {
        return postRepository.findAll()
    }

    @GetMapping("/lookup")
    fun getPostsLookUp() = postRepository.findLookUp()

    @GetMapping("/find")
    fun getPostsFind() = postRepository.find()

    @GetMapping("/find-lazy-false")
    fun getPostsFindLazyFalse() = PostProjectionLookup(postRepository.findOne())

    @GetMapping("/find-lazy-true")
    fun getPostsFindLazyTrue() = PostProjectionLookup(postRepository.findOne())

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

    data class PostProjection(
        val title: String,
        val content: String,
    ) {
        constructor(post: Post) : this(
            title = post.title,
            content = post.content,
        )
    }

    data class AuthorProjection(
        val name: String,
    ) {
        constructor(author: Author) : this(
            name = author.name,
        )
    }

    data class PostProjectionLookup(
        val title: String,
        val content: String,
        val author: AuthorProjection,
    ) {
        constructor(post: Post) : this(
            title = post.title,
            content = post.content,
            author = AuthorProjection(post.author),
        )
    }
}