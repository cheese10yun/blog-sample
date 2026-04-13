package com.example.querydsl.service

import com.example.querydsl.domain.QWriter
import com.example.querydsl.domain.Writer
import com.querydsl.jpa.impl.JPAQueryFactory
import com.querydsl.sql.Configuration
import com.querydsl.sql.MySQLTemplates
import com.querydsl.sql.RelationalPathBase
import com.querydsl.sql.SQLQueryFactory
import javax.sql.DataSource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BatchInsertService(
    private val jpaQueryFactory: JPAQueryFactory,
    private val jdbcTemplate: JdbcTemplate,
    private val dataSource: DataSource
) {

    private val sqlQueryFactory: SQLQueryFactory by lazy {
        SQLQueryFactory(Configuration(MySQLTemplates()), dataSource)
    }

    @Transactional
    fun executeBulkInsertWritersWithSql(writers: List<Writer>): Long {
        // 1. 테이블 메타데이터 정의
        val writerTable = RelationalPathBase(Writer::class.java, "writer", null, "writer")
        // 2. SQLQueryFactory (공유 인스턴스 사용)
        val insert = sqlQueryFactory.insert(writerTable)
        // 3. 데이터를 Batch에 추가
        for (writer in writers) {
            insert.set(QWriter.writer.name, writer.name)
            insert.set(QWriter.writer.email, writer.email)
            insert.set(QWriter.writer.score, 1)
            insert.set(QWriter.writer.reputation, 1.toDouble())
            insert.set(QWriter.writer.active, true)
            insert.addBatch() // 메모리에 쿼리 적재
        }

        // 4. 일괄 실행
        return insert.execute()
    }

    @Transactional
    fun executeBulkUpdateWritersWithSql(writers: List<Writer>): Long {
        // 1. 테이블 메타데이터 정의
        val writerTable = RelationalPathBase(Writer::class.java, "writer", null, "writer")
        // 2. SQLQueryFactory (공유 인스턴스 사용)
        val update = sqlQueryFactory.update(writerTable)
        // 3. 데이터를 Batch에 추가
        for (writer in writers) {
            val id = requireNotNull(writer.id) { "Writer id must not be null" }
            update
                .set(QWriter.writer.name, writer.name)
//                .set(QWriter.writer.email, writer.email)
//                .set(QWriter.writer.score, writer.score)
//                .set(QWriter.writer.reputation, writer.reputation)
//                .set(QWriter.writer.active, writer.active)
                .where(QWriter.writer.id.eq(id))
                .addBatch() // 메모리에 쿼리 적재
        }

        // 4. 일괄 실행
        return update.execute()
    }
}
