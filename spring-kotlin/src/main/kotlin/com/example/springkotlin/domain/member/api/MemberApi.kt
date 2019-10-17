package com.example.springkotlin.domain.member.api

import com.example.springkotlin.domain.member.dao.MemberRepository
import com.example.springkotlin.domain.member.domain.Member
import com.example.springkotlin.domain.member.dto.MemberSignUpRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/members")
class MemberApi(
        private var memberRepository: MemberRepository) {

    @PostMapping
    fun createMember(@RequestBody dto: MemberSignUpRequest): Member {
        return memberRepository.save(dto.toEntity())
    }

//    @GetMapping
//    @Transactional
//    fun getMembers(page: Pageable): List<Member> {
//        println("Transaction Start name : ${TransactionSynchronizationManager.getCurrentTransactionName()}")
//        val members = memberRepository.findAll()
//        val member = members[0]
//        member.updateName(name = UUID.randomUUID().toString())
//        println(member.name)
//
//        println("Transaction End name : ${TransactionSynchronizationManager.getCurrentTransactionName()}")
//        return members
//    }

    @GetMapping
    fun get(page: Pageable): Page<Member>{
        return memberRepository.findAll(page)
    }

    @Transactional
    @GetMapping("/update")
    fun getMembers(page: Pageable): List<Member> {
        val ids = listOf(1L, 2L, 3L, 4L, 5L)
        val count = memberRepository.updateName(ids)
        println("update count : $count")
        return memberRepository.findAll()
    }

//    @GetMapping
//    @Transactional
//    fun getMembers(page: Pageable): List<Member> {
//        val members = memberRepository.findAll()
//
//        for(member in members){
//            member.updateName("none_name")
//        }
//        return members
//    }
}