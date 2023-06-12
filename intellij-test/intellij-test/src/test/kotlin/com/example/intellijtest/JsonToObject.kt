package com.example.intellijtest

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

//data class JsonToObject()


@Entity
@Table(name = "member")
class Member(
    @Column(name = "email", nullable = false, updatable = false)
    val email: String,
    firstName: String,
    lastName: String,
    phoneNumber: String,
    address: String,
    age: Int,
    gender: String,
    occupation: String,
    residentRegistrationNumber: String?,
    status: MemberStatus
) : EntityAuditing() {

    @Column(name = "first_name", nullable = false)
    var firstName: String = firstName
        protected set
    @Column(name = "last_name", nullable = false)
    var lastName: String = lastName
        protected set
    @Column(name = "phone_number", nullable = false)
    var phoneNumber: String = phoneNumber
        protected set
    @Column(name = "address", nullable = false)
    var address: String = address
        protected set
    @Column(name = "age", nullable = false)
    var age: Int =age
        protected set
    @Column(name = "gender", nullable = false)
    var gender: String = gender
        protected set
    @Column(name = "occupation", nullable = false)
    var occupation: String = occupation
        protected set
    @Column(name = "resident_registration_number", nullable = false)
    var residentRegistrationNumber: String? = residentRegistrationNumber
        protected set

    @Enumerated(EnumType.STRING)
    var status: MemberStatus = status
        protected set

    init {
        // 필요하다면 유효성 체크, 기타 로직 수행 등등 진행
    }
}