package com.example.querydsl.domain

import com.example.querydsl.repository.support.QuerydslCustomRepositorySupport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import com.example.querydsl.domain.QWriter.writer as qWriter

@Entity
@Table(name = "writer")
class Writer(
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "email", nullable = false)
    var email: String,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        internal set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    lateinit var createdAt: LocalDateTime
        internal set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    lateinit var updatedAt: LocalDateTime
        internal set
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