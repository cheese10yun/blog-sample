package com.example.mongostudy.dbref

import com.example.mongostudy.mongo.Auditable
import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository

@Document(collection = "post")
class Post(
    @Field(name = "title", targetType = FieldType.STRING)
    val title: String,
    @Field(name = "content", targetType = FieldType.STRING)
    val content: String,

    @DBRef(lazy = false)
    val author: Author,

//    @Field(name = "author_id")
//    val authorId:  ObjectId
) : Auditable() {

    companion object {
        const val DOCUMENT_NAME = "post"
    }
}

interface PostRepository : MongoRepository<Post, ObjectId>, PostCustomRepository

interface PostCustomRepository {
    fun findLookUp(): List<PostProjection>
    fun find(): List<Post>
    fun findOne(): Post
}

class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
) {

    override fun find(): List<Post> {
        return mongoTemplate.findAll<Post>()
    }

    override fun findOne(): Post {
        return mongoTemplate.findOne<Post>(Query())!!
    }

    override fun findLookUp(): List<PostProjection> {
        // 1) $lookup
        val lookupStage = Aggregation.lookup(
            "author",        // from: 실제 컬렉션 이름
            "author.\$id",    // localField: DBRef에서 _id가 들어있는 위치
            "_id",            // foreignField: authors 컬렉션의 _id
            "author"       // as: 결과를 저장할 필드 이름
        )
        // 2) $unwind (optional) - authorDoc을 배열 -> 단일 문서로 변환
        val unwindStage = Aggregation.unwind("author", true)

        val projection = Aggregation.project()
            .andInclude("title")
            .andInclude("content")
            .andInclude("author")
//            .andInclude("updated_at")
//            .andInclude("created_at")

//        val limit = Aggregation.limit(1000)

        // 4) AggregationOptions로 batchSize 설정
//        val options = Aggregation.newAggregationOptions()
//            .cursorBatchSize(2000) // 여기서 batchSize를 지정
//            .build()

        // 3) Aggregation 파이프라인 구성
        val aggregation = Aggregation
            .newAggregation(lookupStage, unwindStage, projection)
//            .withOptions(options)

        // 4) post 컬렉션에서 PostWithAuthor 타입으로 매핑
        return mongoTemplate.aggregate(
            aggregation,
            Post.DOCUMENT_NAME,               // 컬렉션 이름
            PostProjection::class.java
        ).mappedResults
    }
}

class PostProjection(
    val title: String,
    val content: String,
    val author: Author
)

class AuthorProjection(
    val name: String
)