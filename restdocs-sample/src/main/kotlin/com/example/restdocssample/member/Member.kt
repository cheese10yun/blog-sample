package com.example.restdocssample.member

import kotlin.math.min
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull


@RestController
@RequestMapping("/api/members")
class MemberApi(
    private val memberRepository: MemberRepository
) {


    @GetMapping("/{id}")
    fun getMember(@PathVariable id: Long): MemberResponse {
        return MemberResponse(memberRepository.findByIdOrNull(id) ?: throw IllegalArgumentException("$id not fond"))
    }

    @PostMapping
    fun createMember(@RequestBody dto: MemberSignUpRequest) {
        memberRepository.save(dto.toEntity())
    }


    @GetMapping
    fun getMembers(
        @PageableDefault(sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<MemberResponse> {
        return memberRepository.findAll(pageable).map { MemberResponse(it!!) }
    }
}


class MemberResponse(member: Member) {

    @field:NotNull
    @field:NotEmpty
    @field:Length(min = 1, max = 2)
    val id = member.id!!

    @field:NotEmpty
    @field:NotNull
    val email = member.email

    @field:NotEmpty
    @field:NotNull
    @field:Min(value = 2L)
    @field:Max(value = 2222L)
    val name = member.name


    val status = member.status
}


data class MemberSignUpRequest(
    val email: String,
    val name: String,
    val status: MemberStatus
) {


    fun toEntity(): Member {
        return Member(email, name, status)
    }
}


@Entity
@Table(name = "member")
class Member(
    @field:Column(name = "email", nullable = false, unique = true)
    var email: String,
    @field:Column(name = "name", nullable = false)
    var name: String,
    status: MemberStatus
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MemberStatus

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime

    init {
        this.status = status
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    fun modify(name: String) {
        this.name = name
    }
}


interface MemberRepository : JpaRepository<Member, Long>


enum class MemberStatus(description: String) {
    LOCK("일시 정지"),
    NORMAL("정상"),
    BAN("영구 정지");

}