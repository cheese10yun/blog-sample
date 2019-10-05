package com.example.springkotlin.domain.member.domain

import com.example.springkotlin.SpringKotlinApplication
import com.example.springkotlin.domain.member.dao.MemberRepository
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional


@RunWith(SpringRunner::class)
@SpringBootTest(classes = [SpringKotlinApplication::class])
@ActiveProfiles("test")
@Transactional
class MemberTest {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    fun `asdasd`() {



    }
}