package com.spring.test.member

import com.spring.test.EntityAuditing
import javax.persistence.Column
import javax.persistence.Entity

@Entity
class Member(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "email", nullable = false, updatable = true)
    var email: String
) : EntityAuditing() {

}