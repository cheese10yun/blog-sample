package com.spring.test.member

import com.spring.test.SpringTestSupport
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor

//@DataJpaTest
//@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class MemberRepositoryTest(
    private val memberRepository: MemberRepository
) : SpringTestSupport(){

    @Test
    internal fun `find by email test`() {
        memberRepository.findByEmail("asd@asd.com")
    }
}