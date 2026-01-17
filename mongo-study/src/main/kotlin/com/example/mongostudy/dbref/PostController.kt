package com.example.mongostudy.dbref

import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/posts")
class PostController(
    private val postRepository: PostRepository,
    private val mongoTemplate: MongoTemplate,
) {

    @GetMapping
    fun getPosts(
        @PageableDefault pageable: Pageable
    ): Page<Post> {
        return postRepository.findAll(pageable)
    }

//    @GetMapping
//    fun getPosts(
//        @PageableDefault pageable: Pageable
//    ): Page<Post> {
//        return postRepository.findAll(pageable)
//    }

    @GetMapping("/lookup")
    fun getPostsLookUp(@RequestParam(name = "limit") limit: Int): List<Post> {
        return postRepository.findLookUp(limit)
    }

    @GetMapping("/lookup-one")
    fun getPostsLookUp(@RequestParam(name = "id") id: ObjectId): Post? {
        return postRepository.findByIdOrNull(id)
    }

    @GetMapping("/post-with-author")
    fun getPostWithAuthor(@RequestParam(name = "limit") limit: Int): List<Post> {
        return postRepository.find(limit)
    }

    @GetMapping("/post-only")
    fun getPostOnly(@RequestParam(name = "limit") limit: Int): List<PostProjection> = postRepository.find(limit).map { PostProjection(it) }

    @GetMapping("/post-only-one")
    fun getPostOnlyOne() = postRepository.findOne()

    @GetMapping("/post-with-author-one")
    fun getPost() = PostProjection(postRepository.findOne())

//    @GetMapping("/post-with-author")
//    fun getPostWithAuthor() = PostProjectionLookup(postRepository.findOne())

    @GetMapping("/insert")
    fun insert(@RequestParam(name = "limit") limit: Int) {
        (1..limit).map {
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

data class PostProjection(
    val id: ObjectId,
    val title: String,
    val content: String,
) {
    constructor(post: Post) : this(
        id = post.id!!,
        title = post.title,
        content = post.content,
    )
}

data class AuthorProjection(
    val id: ObjectId,
    val name: String,
//    val createdAt: LocalDateTime,
//    val updatedAt: LocalDateTime
)

data class PostProjectionLookup(
    val id: ObjectId,
    val title: String,
    val content: String,
    val author: AuthorProjection,
//    val createdAt: LocalDateTime,
//    val updatedAt: LocalDateTime
)