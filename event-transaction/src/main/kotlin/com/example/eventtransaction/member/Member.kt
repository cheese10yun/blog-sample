package com.example.eventtransaction.member

import com.example.eventtransaction.EntityAuditing
import com.example.eventtransaction.coupon.CouponIssueService
import com.example.eventtransaction.order.EmailSenderService
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "member")
class Member(
    @Column(name = "name", nullable = false)
    var name: String
) : EntityAuditing()

interface MemberRepository : JpaRepository<Member, Long>

@RestController
@RequestMapping("/members")
class MemberApi(
    private val memberSignUpService: MemberSignUpService
) {

    fun signUp(@RequestBody dto: MemberSignUpRequest) {
        memberSignUpService.signUp(dto)
    }
}

@Service
class MemberSignUpService(
    private val memberRepository: MemberRepository,
    private val couponIssueService: CouponIssueService,
    private val emailSenderService: EmailSenderService
) {

    @Transactional
    fun signUp(dto: MemberSignUpRequest) {
        val member = createMember(dto.toEntity())
        emailSenderService.sendSignUp(member)
        couponIssueService.issueSignUpCoupon(member.id!!)
    }

    private fun createMember(member: Member): Member {
        return memberRepository.save(member)
    }
}

data class MemberSignUpRequest(
    val username: String
) {
    fun toEntity(): Member {
        return Member(username)
    }
}