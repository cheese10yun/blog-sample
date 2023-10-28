package com.example.mongostudy.member

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class MemberQueryServiceTest() {


}

@Transactional
@SpringBootTest
class MemberRepositoryTest(
    private val memberRepository: MemberRepository
){


    @Test
    fun `findByEmail asd`() {
        //given
        memberRepository.findByEmail("sample@test.com")

        //when
    }
}