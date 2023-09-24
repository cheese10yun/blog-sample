package com.example.restdocssample.member

import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
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
        if (id == 5L) {
            throw IllegalArgumentException("$id not fond")
        }
        return MemberResponse(memberRepository.findByIdOrNull(id) ?: throw IllegalArgumentException("$id not fond"))
    }

    @PostMapping
    fun createMember(@RequestBody @Valid dto: MemberSignUpRequest) {
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
    @field:Length(min = 1, max = 50)
    val email = member.email

    @field:NotEmpty
    @field:NotNull
    @field:Length(min = 1, max = 50)
    val name = member.name

    val status = member.status
}

data class MemberSignUpRequest(
    @field:Length(min = 1, max = 50)
    @field:NotEmpty
    val email: String,

    @field:Length(min = 1, max = 22)
    @field:NotEmpty
    val name: String,

    @field:NotNull
    val status: MemberStatus
) {

    fun toEntity(): Member {
        return Member(email, name, status)
    }
}