package com.example.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Email

@RestController
@RequestMapping("/sample")
class SampleApi {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun sample(@RequestBody @Valid sample: SampleRequest) = sample
}


data class SampleRequest(
    @field:Valid
    val emails: List<AAA>
)

data class AAA(
    @field:Email
    val email: String
)


@RestController
@RequestMapping("/members")
class MemberApi(
    private val memberRepository: MemberRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getMembers(pageable: Pageable): Page<Member> {
        val members = memberRepository.findAll(pageable)
        logger.info(objectMapper.writeValueAsString(members))
        return members
    }
}

data class Sample(
    val name: String,
    val age: Int
)



