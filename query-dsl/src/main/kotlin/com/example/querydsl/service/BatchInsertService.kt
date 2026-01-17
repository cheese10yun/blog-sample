package com.example.querydsl.service

import com.example.querydsl.domain.Member
import com.example.querydsl.domain.QMember
import com.example.querydsl.domain.Writer
import com.example.querydsl.domain.QWriter
import com.example.querydsl.domain.SWriter
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.MySQLTemplates
import com.querydsl.core.types.PathMetadataFactory
import com.querydsl.sql.RelationalPathBase
import com.querydsl.core.types.dsl.StringPath
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.sql.Configuration
import javax.sql.DataSource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.StopWatch // StopWatch import 추가

@Service
class BatchInsertService(
    private val jpaQueryFactory: JPAQueryFactory,
    private val jdbcTemplate: JdbcTemplate,
    private val dataSource: DataSource // Inject DataSource
) {

//    @Transactional
//    fun executeBulkInsert(members: List<Member>): Long {
//        val qMember = QMember.member
//
//        var insertClause = jpaQueryFactory.insert(qMember)
//
//        for (member in members) {
//            insertClause = insertClause.set(qMember.username, member.username)
//                .set(qMember.age, member.age)
//                .set(qMember.status, member.status)
//                .set(qMember.team, member.team)
//                .addBatch()
//        }
//
//        return insertClause.execute()
//    }

    @Transactional
    fun executeBulkInsertWithSql(members: List<Member>): Long {
        // Define the table path dynamically for SQL operations
        val memberTable = RelationalPathBase(Member::class.java, "member", null, "member")

        val username = Expressions.stringPath(memberTable, "username")
        val age = Expressions.numberPath(Int::class.java, memberTable, "age")
        val status = Expressions.stringPath(memberTable, "status")
        val teamId = Expressions.numberPath(Long::class.java, memberTable, "team_id") // Assuming team_id is a Long

        val sqlQueryFactory = SQLQueryFactory(com.querydsl.sql.Configuration(MySQLTemplates()), dataSource)

        val insert = sqlQueryFactory.insert(memberTable)

        for (member in members) {
            insert.set(username, member.username)
            insert.set(age, member.age)
            insert.set(status, member.status.name) // Assuming status is stored as String in DB
            insert.set(teamId, member.team.id) // Assuming team has an 'id' property
            insert.addBatch()
        }
        return insert.execute()
    }


    @Transactional
    fun executeBulkInsertWritersWithSql(writers: List<Writer>): Long {
        val writerTable = RelationalPathBase(Writer::class.java, "writer", null, "writer")
        val sqlQueryFactory = SQLQueryFactory(Configuration(MySQLTemplates()), dataSource)
        val insert = sqlQueryFactory.insert(writerTable)
        for (writer in writers) {
            insert.set(QWriter.writer.name, writer.name)
            insert.set(QWriter.writer.email, writer.email)
            insert.set(QWriter.writer.score, 1)
            insert.set(QWriter.writer.reputation, 1.toDouble())
            insert.set(QWriter.writer.active, true)
            insert.addBatch()
        }
        return insert.execute()
    }

//    fun bulkInsertWriters(writers: List<Writer>, ): Long {
//        val sqlQueryFactory = SQLQueryFactory(
//            Configuration(MySQLTemplates()),
//            dataSource
//        )
//        val insert = sqlQueryFactory.insert(SWriter)
//        writers.forEach {
//            insert.set(SWriter.name, it.name)
//                .set(SWriter.email, it.email)
//                .set(SWriter.score, 1)
//                .set(SWriter.reputation, 1.0)
//                .set(SWriter.active, true)
//                .addBatch()
//        }
//        return insert.execute()
//    }
}
