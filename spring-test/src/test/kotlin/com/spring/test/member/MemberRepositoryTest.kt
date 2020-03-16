package com.spring.test.member

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestConstructor

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class MemberRepositoryTest(
    private val memberRepository: MemberRepository
) {

    @Test
    internal fun `find by email test`() {
        memberRepository.findByEmail("asd@asd.com")
    }
}