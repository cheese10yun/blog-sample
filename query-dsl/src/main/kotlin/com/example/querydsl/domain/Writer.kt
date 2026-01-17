package com.example.querydsl.domain
// Kotlin
import com.querydsl.sql.ColumnMetadata
import com.querydsl.sql.RelationalPathBase
import com.querydsl.core.types.dsl.*

import com.example.querydsl.repository.support.QuerydslCustomRepositorySupport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import com.example.querydsl.domain.QWriter.writer as qWriter

@Entity
@Table(name = "writer")
class Writer(
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "score", nullable = false)
    var score: Int = 0,

    @Column(name = "reputation", nullable = false)
    var reputation: Double = 0.0,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

//    @Column(name = "last_login")
//    var lastLogin: LocalDate? = null,
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "writer_type", nullable = false)
//    var writerType: WriterType = WriterType.ROOKIE,
): EntityAuditing() {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    var id: Long? = null
//        internal set
//
//    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
//    lateinit var createdAt: LocalDateTime
//        internal set
//
//    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
//    lateinit var updatedAt: LocalDateTime
//        internal set
}

enum class WriterType {
    ROOKIE, VETERAN, MASTER
}

interface WriterRepository : JpaRepository<Writer, Long>, WriterCustomRepository

interface WriterCustomRepository {

    fun update(ids: List<Long>)
}

class WriterCustomRepositoryImpl : QuerydslCustomRepositorySupport(Writer::class.java), WriterCustomRepository {

    @Transactional
    override fun update(ids: List<Long>) {
        for (id in ids) {
            update(qWriter)
                .set(qWriter.name, "update")
                .where(qWriter.id.eq(id))
                .execute()
        }
    }
}


@Service
@Transactional
class WriterService(
    private val writerRepository: WriterRepository
) {


    @Transactional
    fun findAll(): List<Writer> = writerRepository.findAll()

    @Transactional
    fun update(writers: List<Writer>) {
        for (writer in writers) {
            writer.name = "update"
        }
    }

    @Transactional
    fun nonPersistContestUpdate(ids: List<Long>) {
        writerRepository.update(ids)
    }

}


object SWriter : RelationalPathBase<Any>(Any::class.java, "writer", null, "writer") {
    private fun readResolve(): Any = SWriter
    val id: NumberPath<Long> = createNumber("id", Long::class.java)
    val name: StringPath = createString("name")
    val email: StringPath = createString("email")
    val score: NumberPath<Int> = createNumber("score", Int::class.java)
    val reputation: NumberPath<Double> = createNumber("reputation", Double::class.java)
    val active: BooleanPath = createBoolean("active")

    init {
        addMetadata(id, ColumnMetadata.named("id"))
        addMetadata(name, ColumnMetadata.named("name"))
        addMetadata(email, ColumnMetadata.named("email"))
        addMetadata(score, ColumnMetadata.named("score"))
        addMetadata(reputation, ColumnMetadata.named("reputation"))
        addMetadata(active, ColumnMetadata.named("active"))
    }
}

fun bulkInsertWriters(writers: List<Writer>, dataSource: javax.sql.DataSource): Long {
    val sqlQueryFactory = com.querydsl.sql.SQLQueryFactory(
        com.querydsl.sql.Configuration(com.querydsl.sql.MySQLTemplates()),
        dataSource
    )
    val insert = sqlQueryFactory.insert(SWriter)
    writers.forEach {
        insert.set(SWriter.name, it.name)
            .set(SWriter.email, it.email)
            .set(SWriter.score, 1)
            .set(SWriter.reputation, 1.0)
            .set(SWriter.active, true)
            .addBatch()
    }
    return insert.execute()
}