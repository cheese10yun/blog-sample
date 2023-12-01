package com.example.restdocssample

import com.example.restdocssample.member.Member
import com.example.restdocssample.member.MemberStatus
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class MemberKtorClientTest {

    private val memberClient: MemberKtorClient = MemberKtorClient()

    @Test
    fun `getOrThrow notnull 보장, 오류 발생시 오류 메시지를 그대로 전달`() {
        memberClient
            .getMember(1L)
            .getOrThrow()
    }


//    @Test
//    fun `getOrNull 통신 실패시 null 응답,`() {
//        val member: Member? = memberClient
//                .getMember(1L)
//                .getOrNull { it }
//
//        // member null 여부에 따른 후속 조치 작업 진행
//    }

    @Test
    fun `onFailure + onSuccess`() {
        val orThrow: ResponseResult<Member> = memberClient
            .getMember(1L)
            .onFailure { it: ErrorResponse ->
                // onFailure 오류 발생시 ErrorResponse 기반으로 예외 처리 진행
            }
            .onSuccess {
                it
            }
    }

    @Test
    fun `필수 값이 아닌 경우`() {
        val member: Member? = memberClient
            .getMember(1L)
            .getOrNull()
    }

    @Test
    fun `필수 값인 케이스 - 예외 발생 Notnull 보장`() {
        val member: Member = memberClient
            .getMember(1L)
            .getOrThrow()
    }

    @Test
    fun `isSuccess + isFailure`() {
        // API PUT, POST 등에 사용
        val result1 = memberClient
            .getMember(1L)
            .isSuccess

        val result2 = memberClient
            .getMember(1L)
            .isFailure
    }

    @Test
    fun `getOrDefault - 통시 실패시 기본 값 할당`() {
        val member: Member = memberClient
            .getMember(1L)
            .getOrDefault(
                default = Member(
                    email = "sample@sample.com",
                    name = "sample",
                    status = MemberStatus.NORMAL
                ),
                transform = { it }
            )
    }

    @Test
    fun `조합`() {
        val member = memberClient
            .getMember(1L)
            .onFailure { it: ErrorResponse ->
                // 실패 케이스 보상 트랜잭션 API 호출
            }
            .onSuccess { it: Member ->
                // 성공 이후 후속 작업 진행
            }
            .getOrDefault(
                // 혹시라도 오류 발생시 기본 값 응답
                default = Member(
                    email = "",
                    name = "",
                    status = MemberStatus.NORMAL

                ),
                transform = { it }
            )
    }
}