package com.example.exposedstudy

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "member")
class Member(
        @Column(name = "name", nullable = false)
        var name: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

interface MemberRepository : JpaRepository<Member, Long>
