package com.example.intellijtest

import com.example.intellijtest.QMember.member
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service

@Entity
@Table(name = "member")
//@Where(clause = "status = 'NORMAL'")
class Member(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val age: Int,
    val gender: String,
    val occupation: String,
    val residentRegistrationNumber: String?,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: MemberStatus = MemberStatus.NORMAL
) : EntityAuditing()

enum class MemberStatus(
    desc: String
) {
    NORMAL("정상"),
    UNVERIFIED("미인증"),
    LOCK("계정 일지 정지"),
    BAN("계정 영구정지");
}

interface MemberRepository :
    JpaRepository<Member, Long>,
    MemberRepositoryCustom

interface MemberRepositoryCustom {
    // 특정 성별의로 유저 조회
    fun findBy(gender: String): List<Member>
    fun findBy(age: Int): List<Member>
    fun existedEmail(email: String): Boolean
    fun existedPhoneNumber(phoneNumber: String): Boolean
}

class MemberRepositoryImpl :
    QuerydslCustomRepositorySupport(Member::class.java),
    MemberRepositoryCustom {

    override fun findBy(gender: String): List<Member> =
        selectFrom(member)
            .where(member.status.`in`(MemberStatus.NORMAL, MemberStatus.UNVERIFIED))
            .fetch()

    override fun findBy(age: Int): List<Member> =
        selectFrom(member)
            .where(member.status.`in`(MemberStatus.NORMAL, MemberStatus.UNVERIFIED))
            .where(member.age.gt(age))
            .fetch()

    override fun existedEmail(email: String) =
        select(member.id)
            .from(member)
            .where(member.email.eq(email))
            .fetchFirst() != null

    override fun existedPhoneNumber(phoneNumber: String) =
        select(member.id)
            .from(member)
            .where(member.phoneNumber.eq(phoneNumber))
            .fetchFirst() != null
}


@Service
class MemberQueryService(
    private val memberRepository: MemberRepository,
) {

    fun findActivityMemberBy(gender: String): List<Member> =
        memberRepository.findBy(gender)

    fun existedEmail(email: String) = memberRepository.existedEmail(email)

    fun existedPhoneNumber(phoneNumber: String) = memberRepository.existedPhoneNumber(phoneNumber)
}

data class AdultMember(
    // ...
    val residentRegistrationNumber: String,
    var status: MemberStatus,
) {
}
