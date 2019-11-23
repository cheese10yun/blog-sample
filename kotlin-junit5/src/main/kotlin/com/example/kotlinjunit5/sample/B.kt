package com.example.kotlinjunit5.sample

import javax.persistence.*

@Entity
@Table(name = "B")
class B(
        @Column(name = "name")
        val name: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

}