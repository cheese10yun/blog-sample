package com.example.querydsl

import com.example.querydsl.domain.Hello
import com.example.querydsl.domain.QHello
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class QueryDslApplicationTests(
        private val em: EntityManager
) {

    @Test
    internal fun `querydsl setting test`() {
        val hello = Hello("yun")
        em.persist(hello)

        val query = JPAQueryFactory(em)

        val qHello = QHello.hello

        val findHello = query
                .selectFrom(qHello)
                .where(qHello.name.eq("yun"))
                .fetchOne()!!

        then(findHello.id).isNotNull()
        then(findHello.name).isEqualTo("yun")
    }
}
