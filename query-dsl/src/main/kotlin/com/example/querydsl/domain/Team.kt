package com.example.querydsl.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "team")
data class Team(
    @Column(name = "name", nullable = false)
    var name: String
) : EntityAuditing() {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "team")
    var members: MutableList<Member> = mutableListOf()
}