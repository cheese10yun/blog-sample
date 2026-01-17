package com.example.boot3mongo.dbref

import com.example.boot3mongo.Boot3MongoApplicationTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

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

    @Test
    fun `save 동작시 엔티티에 정의되지 않은 필드가 덮어씌여지는지`() {

        val post = postRepository.findByIdOrNull(ObjectId("67cd82c4aec68267745dd36d"))!!

        post.title = "new new title"


        mongoTemplate.save(post)


        val query: Query = Query(Criteria.where("_id").`is`("user_1"))
        val update: Update = Update().set("age", 31) // 나이만 변경하겠다고 명시
        mongoTemplate.updateFirst(query, update, User::class.java)

    }

    fun bulkUpdateUserGrades(targetIds: List<String>) {
        // 1. Query 생성: { "_id": { "$in": ["...", "..."] } }
        // 주의: Kotlin에서 'in'은 예약어이므로 백틱(`)으로 감싸야 합니다.
        val query = Query(Criteria.where("_id").`in`(targetIds))
        // 2. Update 생성: { "$set": { "grade": "BASIC" } }
        val update = Update().set("grade", "BASIC")
        // 3. 실행: updateMany -> updateMulti
        // updateFirst는 1건만, updateMulti는 조건에 맞는 모든 문서를 수정합니다.
        val result = mongoTemplate.updateMulti(query, update, User::class.java)
        // 결과 확인 (로그 등)
        println("매칭된 문서 수: ${result.matchedCount}")
        println("실제 수정된 문서 수: ${result.modifiedCount}")
    }

    fun updateBulk(
        ids: List<ObjectId>,
        // BulkOperations.BulkMode.UNORDERED // or BulkOperations.BulkMode.ORDERED
        bulkMode: BulkOperations.BulkMode
    ): BulkWriteResult {
        val bulkOps = mongoTemplate.bulkOps(bulkMode, User::class.java)
        for (id in ids) {
            bulkOps.updateOne(
                Query(Criteria.where("_id").`is`(id)),
                Update().set("point", 23.3)
            )
        }
        return bulkOps.execute()
    }
}


@Document(collection = "post")
class Post(
    @Id
    var id: ObjectId? = null,
    @Field(name = "title")
    val title: String,
    @Field(name = "content")
    val content: String,
    // DB Ref 방식
    @DBRef(lazy = false)
    val author: Author,
    // ID 참조 방식
    val authorId: ObjectId
)

@Document(collection = "author")
class Author(
    @Id
    var id: ObjectId? = null,
    @Field(name = "name")
    val name: String
)