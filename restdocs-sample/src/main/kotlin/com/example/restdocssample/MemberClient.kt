package com.example.restdocssample

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MemberClient(
    private val restTemplate: RestTemplate
) {

    fun getMember(memberId: Long): ResponseEntity<Member> {
        val url = "http://example.com/api/members/$memberId"
        // GET 요청을 보내고 ResponseEntity로 응답을 받음
        return restTemplate.getForEntity(url, Member::class.java)
    }

    fun getMember2(memberId: Long): Member {
        val url = "http://example.com/api/members/$memberId"
        // GET 요청을 보내고 ResponseEntity로 응답을 받음
        return restTemplate.getForObject(url, Member::class.java)!!
    }
}

fun xxx() {
    val memberClient = MemberClient(RestTemplate())
    val response = memberClient.getMember(1L) // 1번 회원 조회를 가정


    if (response.statusCode.is2xxSuccessful) {
        // 비즈니스 로직
    } else {
        // 2xx가 아닌 경우의 처리 로직
    }

    val success = Result.success("asd")

    Result.failure<String>(exception = )

}


data class Member(
    val id: Long,
    val name: String,
    val email: String
)
