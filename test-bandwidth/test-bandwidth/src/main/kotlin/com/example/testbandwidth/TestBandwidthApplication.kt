package com.example.testbandwidth

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@SpringBootApplication
class TestBandwidthApplication

fun main(args: Array<String>) {
    runApplication<TestBandwidthApplication>(*args)
}


@Entity
class Member(
    @Column(name = "name", nullable = false)
    var name: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null
}

@Service
@Transactional(readOnly = true)
class MemberFindService(
    private val memberRepository: MemberRepository
) {
    fun findById(id: Long): Member {
        return memberRepository.findById(id).orElseThrow { IllegalArgumentException("not found") }
    }

    fun findByName(name: String?): Member {
        return memberRepository.findByName(name)
    }
}


interface MemberRepository : JpaRepository<Member, Long> {
    fun findByName(name: String?): Member
}
