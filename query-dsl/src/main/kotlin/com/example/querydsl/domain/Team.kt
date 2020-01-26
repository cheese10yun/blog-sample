package com.example.querydsl.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "team")
data class Team(
        @Column(name = "name", nullable = false)
        var name: String
) : EntityAuditing() {

    @OneToMany(mappedBy = "team")
    var members: MutableList<Member> = mutableListOf()

}