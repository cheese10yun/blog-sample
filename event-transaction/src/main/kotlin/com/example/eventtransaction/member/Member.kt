package com.example.eventtransaction.member

import com.example.eventtransaction.EntityAuditing
import com.example.eventtransaction.coupon.CouponIssueService
import com.example.eventtransaction.order.EmailSenderService
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
        val member = createMember(dto.toEntity())
//        emailSenderService.sendSignUpEmail(member)
        eventPublisher.publishEvent(MemberSignedUpEvent(member))
        couponIssueService.issueSignUpCoupon(member.id!!)
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

    @TransactionalEventListener
//    @EventListener
    fun memberSignedUpEventListener(event: MemberSignedUpEvent) {
        emailSenderService.sendSignUpEmail(event.member)
    }
}