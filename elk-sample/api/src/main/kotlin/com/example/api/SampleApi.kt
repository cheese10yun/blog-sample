package com.example.api

import com.fasterxml.jackson.databind.ObjectMapper
import javax.validation.Valid
import javax.validation.constraints.Email
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class SampleApi {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun sample(@RequestBody @Valid sample: SampleRequest) = sample
}

@Service
class MemberService(
    val memberRepository: MemberRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun memberxxxx(pageable: Pageable): Page<Member> {

        aaaa()
        bbbb()
        val members = memberRepository.findAll(pageable)
        logger.info(objectMapper.writeValueAsString(members))

        return members

    }

    fun aaaa(){
        memberRepository.findById(1L)
        memberRepository.findById(2L)
        memberRepository.findById(3L)

    }

    fun bbbb(){
        Thread.sleep(100)
    }
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
    private val memeberService: MemberService
) {


    @GetMapping
    fun getMembers(pageable: Pageable) = memeberService.memberxxxx(pageable)


    @GetMapping("/test")
    fun asd() {
        if (true) {
            throw IllegalStateException("server error test error")
        }
    }
}

data class Sample(
    val name: String,
    val age: Int
)