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
    firstName: String,
    lastName: String,
    email: String,
    phoneNumber: String,
    address: String,
    age: Int,
    gender: String,
    occupation: String,
    residentRegistrationNumber: String?,
    status: MemberStatus
) : EntityAuditing() {

    var firstName: String = ""
        internal set
    var lastName: String = ""
        internal set
    var email: String = ""
        internal set
    var phoneNumber: String = ""
        internal set
    var address: String = ""
        internal set
    var age: Int = 0
        internal set
    var gender: String = ""
        internal set
    var occupation: String = ""
        internal set
    var residentRegistrationNumber: String? = null
        internal set
    @Enumerated(EnumType.STRING)
    var status: MemberStatus = MemberStatus.NORMAL


    init {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.phoneNumber = phoneNumber
        this.address = address
        this.age = age
        this.gender = gender
        this.occupation = occupation
        this.residentRegistrationNumber = residentRegistrationNumber
        this.status = status

    }
}

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
