package com.example.mongostudy.member

import com.example.mongostudy.MongoDataSetup
import com.example.mongostudy.MongoStudyApplicationTests
import com.example.mongostudy.MongoTestSupport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.transaction.annotation.Transactional


class MemberQueryServiceTest : MongoStudyApplicationTests() {

    @Test
    fun adasdd() {

        println()
    }
}


@MongoTestSupport
class MemberRepositoryTest(
    private val memberRepository: MemberRepository
) : MongoStudyApplicationTests() {

    @MongoDataSetup(
        jsonPath = "/",
        clazz = Member::class
    )
    @Test
    fun `findByEmail asd`() {
        //given
        memberRepository.findByEmail("sample@test.com")
        //when
    }


}