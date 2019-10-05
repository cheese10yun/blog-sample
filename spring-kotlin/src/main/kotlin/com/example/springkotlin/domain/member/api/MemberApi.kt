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

//    @GetMapping
//    @Transactional
//    fun getMembers(page: Pageable): List<Member> {
//        val members = memberRepository.findAll()
//
//        val ids = mutableListOf<Long>()
//
//        for(member in members){
//            ids.add(member.id)
//        }
//
//        val count = memberRepository.updateName(ids)
//        println(count)
//
//        return members
//    }

    @GetMapping
    @Transactional
    fun getMembers(page: Pageable): List<Member> {
        val members = memberRepository.findAll()

        for(member in members){
            member.updateName("none_name")
        }
        return members
    }
}