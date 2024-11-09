package com.example.redis

import kotlin.jvm.optionals.getOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping()
class MemberController(
    private val memberRepository: MemberRepository,
    private val addressRepository: AddressRepository
) {
    @GetMapping("/api/members")
    fun getMember(): Member? {
        memberRepository.findById(1).getOrNull()
        memberRepository.findById(2).getOrNull()
        memberRepository.findById(3).getOrNull()
        memberRepository.findById(4).getOrNull()
        memberRepository.findById(5).getOrNull()
        memberRepository.findById(6).getOrNull()
        memberRepository.findById(7).getOrNull()
        memberRepository.findById(8).getOrNull()
        memberRepository.findById(9).getOrNull()
        memberRepository.findById(10).getOrNull()
        memberRepository.findById(11).getOrNull()
        memberRepository.findById(12).getOrNull()
        memberRepository.findById(13).getOrNull()
        memberRepository.findById(14).getOrNull()
        memberRepository.findById(15).getOrNull()
        memberRepository.findById(16).getOrNull()
        memberRepository.findById(17).getOrNull()
        memberRepository.findById(18).getOrNull()
        memberRepository.findById(19).getOrNull()
        memberRepository.findById(20).getOrNull()
        memberRepository.findById(21).getOrNull()
        memberRepository.findById(22).getOrNull()
        memberRepository.findById(23).getOrNull()
        memberRepository.findById(24).getOrNull()
        memberRepository.findById(25).getOrNull()
        memberRepository.findById(26).getOrNull()
        memberRepository.findById(27).getOrNull()
        memberRepository.findById(28).getOrNull()
        memberRepository.findById(29).getOrNull()
        memberRepository.findById(30).getOrNull()
        memberRepository.findById(31).getOrNull()
        memberRepository.findById(32).getOrNull()
        memberRepository.findById(33).getOrNull()
        memberRepository.findById(34).getOrNull()
        memberRepository.findById(35).getOrNull()
        return memberRepository.findById(42).getOrNull()
//        return memberRepository.findAll().toList()
    }

    @GetMapping("/api/address")
    fun getAddress(): List<Address> {

        return addressRepository.findAll().toList()
    }
}