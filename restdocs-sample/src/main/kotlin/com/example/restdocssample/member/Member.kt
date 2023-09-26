package com.example.restdocssample.member

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table


@Entity
@Table(name = "member")
class Member(
    @field:Column(name = "email", nullable = false, unique = true)
    var email: String,
    @field:Column(name = "name", nullable = false)
    var name: String,
    status: MemberStatus
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MemberStatus

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime

    init {
        this.status = status
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    fun modify(name: String) {
        this.name = name
    }

    override fun toString(): String {
        return "Member(email='$email', name='$name', id=$id, status=$status, createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}

interface MemberRepository : JpaRepository<Member, Long>


enum class MemberStatus(description: String) {
    LOCK("일시 정지"),
    NORMAL("정상"),
    BAN("영구 정지");

}