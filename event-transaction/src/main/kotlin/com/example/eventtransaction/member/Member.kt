package com.example.eventtransaction.member

import com.example.eventtransaction.EntityAuditing
import com.example.eventtransaction.coupon.CouponIssueService
import com.example.eventtransaction.EmailSenderService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.bind.annotation.PostMapping
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

    @PostMapping
    fun signUp(@RequestBody dto: MemberSignUpRequest) {
        memberSignUpService.signUp(dto)
    }
}

@Service
class MemberSignUpService(
    private val memberRepository: MemberRepository,
    private val couponIssueService: CouponIssueService,
    private val emailSenderService: EmailSenderService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun signUp(dto: MemberSignUpRequest) {
        val member = createMember(dto.toEntity()) // 1. member 엔티티 영속화
//        emailSenderService.sendSignUpEmail(member) // 2. 외부 시스템 이메일 호출
        eventPublisher.publishEvent(MemberSignedUpEvent(member)) //2. 회원 가입 완료 이벤트 발행
        couponIssueService.issueSignUpCoupon(member.id!!) // 3. 회원가입 쿠폰 발급
    }

    private fun createMember(member: Member): Member {
        return memberRepository.save(member)
    }
}

data class MemberSignUpRequest(
    val name: String
) {
    fun toEntity(): Member {
        return Member(name)
    }
}

data class MemberSignedUpEvent(
    val member: Member
)

@Component
class MemberEventHandler(
    private val emailSenderService: EmailSenderService
) {

    // 회원가입 완료 이벤트 리스너 Bean 등록
    @TransactionalEventListener
//    @EventListener
    fun memberSignedUpEventListener(event: MemberSignedUpEvent) {
        emailSenderService.sendSignUpEmail(event.member)
    }
}