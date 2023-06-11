package com.example.intellijtest

import com.example.intellijtest.QMember.member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service

interface GeneralMember {

    val firstName: String
    val lastName: String

    // ...
    fun fullName(): String {
        return "$firstName $lastName"
    }
}


@Entity
@Table(name = "member")
class Member(
    @Column(name = "email", nullable = false, updatable = false)
    val email: String,
    firstName: String,
    lastName: String,
    phoneNumber: String,
    address: String,
    age: Int,
    gender: String,
    occupation: String,
    residentRegistrationNumber: String?,
    status: MemberStatus
) : EntityAuditing(), GeneralMember {

    @Column(name = "first_name", nullable = false)
    override var firstName: String = ""
        protected set

    @Column(name = "last_name", nullable = false)
    override var lastName: String = ""
        protected set

    @Column(name = "phone_number", nullable = false)
    var phoneNumber: String = ""
        protected set

    @Column(name = "address", nullable = false)
    var address: String = ""
        protected set

    @Column(name = "age", nullable = false)
    var age: Int = 0
        protected set

    @Column(name = "gender", nullable = false)
    var gender: String = ""
        protected set

    @Column(name = "occupation", nullable = false)
    var occupation: String = ""
        protected set

    @Column(name = "resident_registration_number", nullable = false)
    var residentRegistrationNumber: String? = null
        protected set

    @Enumerated(EnumType.STRING)
    var status: MemberStatus = MemberStatus.NORMAL

    init {
        // 필요하다면 유효성 체크, 기타 로직 수행 등등 진행
        this.firstName = firstName
        this.lastName = lastName
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
    override val firstName: String,
    override val lastName: String,
    val status: MemberStatus,
) : GeneralMember
