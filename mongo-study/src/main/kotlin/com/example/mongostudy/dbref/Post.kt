package com.example.mongostudy.dbref

import com.example.mongostudy.mongo.Auditable
import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository

@Document(collection = "post")
class Post(
    @Id
    var id: ObjectId? = null,
    @Field(name = "title", targetType = FieldType.STRING)
    val title: String,
    @Field(name = "content", targetType = FieldType.STRING)
    val content: String,
    @DBRef(lazy = true)
    val author: Author,
) {
    companion object {
        const val DOCUMENT_NAME = "post"
    }
}

interface PostRepository : MongoRepository<Post, ObjectId>, PostCustomRepository

interface PostCustomRepository {
    fun findLookUp(limit: Int): List<Post>
    fun find(limit: Int): List<Post>
    fun findOne(): Post
    fun findByIdOrNull(id: ObjectId): Post?
}

class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
) {

    override fun findByIdOrNull(id: ObjectId): Post? {
        val match = Aggregation.match(Criteria.where("_id").`is`(id))
        val lookupStage = Aggregation.lookup(
            "author",        // from: 실제 컬렉션 이름
            "author.\$id",    // localField: DBRef에서 _id가 들어있는 위치
            "_id",            // foreignField: authors 컬렉션의 _id
            "author"       // as: 결과를 저장할 필드 이름
        )
        val unwindStage = Aggregation.unwind("author", true)
        val projection = Aggregation.project()
            .andInclude("title")
            .andInclude("content")
            .andInclude("author")
        val aggregation = Aggregation.newAggregation(match, lookupStage, unwindStage, projection)
        return mongoTemplate
            .aggregate(
                aggregation,
                "post",
                Post::class.java,
            )
            .uniqueMappedResult
    }

    override fun find(limit: Int): List<Post> {
        return mongoTemplate.find(Query().limit(limit))
    }

    override fun findOne(): Post {
        return mongoTemplate.findOne<Post>(Query())!!
    }

    override fun findLookUp(limit: Int): List<Post> {
        val lookupStage = Aggregation.lookup(
            "author",        // from: 실제 컬렉션 이름
            "author.\$id",    // localField: DBRef에서 _id가 들어있는 위치
            "_id",            // foreignField: authors 컬렉션의 _id
            "author"       // as: 결과를 저장할 필드 이름
        )
        val unwindStage = Aggregation.unwind("author", true)
        val projection = Aggregation.project()
            .andInclude("title")
            .andInclude("content")
            .andInclude("author")
        val limitStage = Aggregation
            .limit(limit.toLong())
        val aggregation = Aggregation
            .newAggregation(lookupStage, unwindStage, projection, limitStage)
        return mongoTemplate
            .aggregate(
                aggregation,
                Post.DOCUMENT_NAME,
                Post::class.java,
            )
            .mappedResults
    }
}