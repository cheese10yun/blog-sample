package com.example.exposedstudy

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest(properties = ["spring.profiles.active=test"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
//@Transactional // 롤백 확실
//@Transactional // 디비 insert 까지 한다. @Transactional 유뮤 없이
class FooTest(
    private val fooJpaRepository: FooRepository,
    private val entityManager: EntityManager
) {

    @Test
    @Sql("/setup.sql")
    fun name() {

        // 플러시 모드 오토 -> 이 케이스만 생각
        // flush 3 케이스
        // 1. JPQL 만나면 자동으로 플러쉬
        // 2. 트랜잭션 commit시 자동으로 플러쉬 -> //  then -> query dsl -> insert 실제 디비에 한다.
        // 3. 강제로 시키는 케이스 ?

        //  given save() -> 실제 디비 insert 하냐 안하냐 ?
        //  then -> query dsl -> insert 실제 디비에 한다.
        //  then -> X ??? -> insert 실제 디비에 한다.

        // 결론은 SQL 기반으로 테스트 or batch jpa or expoesd

        val save = fooJpaRepository.save(Foo("name"))

//        val findByTitle = fooJpaRepository.findByTitle("111")
//        fooJpaRepository.flush()
//        entityManager.flush()
//        entityManager.clear()

        println("")


        // jpql X 실제 X
    }

    @Test
    @Sql("/setup.sql")
    fun `sql test`() {

        /**
         * @Transactional 없는 경우
         * 1. auto_commit = false 변경
         * 2. insert into 진행
         * 3. commit
         *
         * @Transactional 있는 경우
         * 1. auto_commit = false 변경
         * 2. insert into 진행
         * 3. commit 없음
         */


//        val save = fooJpaRepository.save(Foo("name"))


        println("")


        // jpql X 실제 X
    }
}