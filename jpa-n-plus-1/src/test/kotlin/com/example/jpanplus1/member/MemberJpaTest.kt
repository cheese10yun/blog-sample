package com.example.jpanplus1.member

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
internal class MemberJpaTest(
        val memberRepository: MemberRepository
) {

    @Test
    internal fun `즉시로딩 n+1`() {
        val members = memberRepository.findAll()
    }

    @Test
    internal fun `지연로딩인 n+1`() {
        val members = memberRepository.findAll()

        // 회원 한명에 대한 조회는 문제가 없다
        val firstMember = members[0]
//        println("order size : ${firstMember.orders.size}")

        // 조회한 모든 회원에 대해서 조회하는 경우 문제 발생
        for (member in members) {
            println("order size of member ${member.id}: ${member.orders.size}")
        }
    }

    @Test
    internal fun `페치 조인 사용`() {
        val members = memberRepository.findAllWithFetch()

        // 조회한 모든 회원에 대해서 조회하는 경우에도 N+1 문제가 발생하지 않음
        for (member in members) {
            println("order size: ${member.orders.size}")
        }
    }

    @Test
    internal fun `컬렉션을 페치 조인하면 페이징 API를 사용할 수 없다`() {
        val page = PageRequest.of(0, 10)
        val members = memberRepository.findAllWithFetchPaging(page)

        // 조회한 모든 회원에 대해서 조회하는 경우에도 N+1 문제가 발생하지 않음
        for (member in members) {
            println("order size: ${member.orders.size}")
        }
    }

    @Test
    internal fun `둘 이상 컬렉션을 페치할 수 없다`() {
        val page = PageRequest.of(0, 10)
        val members = memberRepository.findAllWithFetchPaging2(page)

        // 조회한 모든 회원에 대해서 조회하는 경우에도 N+1 문제가 발생하지 않음
        for (member in members) {
            println("order size: ${member.orders.size}")
        }
    }

    @Test
    internal fun `QueryDsl Projections`() {
        val members = memberRepository.findMemberAll()

        for (member in members) {
            println(member)
        }
    }

    @Test
    @Sql("/member-data.sql")
    internal fun `sql test`() {
        val members = memberRepository.findAll()

        for (member in members) {
            println(member.id)
        }
    }

    @Test
    internal fun `QueryDsl Projections2`() {
        memberRepository.findMemberww()
    }
}