package com.example.exposedstudy

import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "foo")
class Foo(
    @Column(name = "title", nullable = false)
    var title: String,
) : AuditingEntityId()

interface FooRepository : JpaRepository<Foo, Long>{
    fun findByTitle(title: String): List<Foo>
}

@MappedSuperclass
@EntityListeners(value = [AuditingEntityListener::class])
abstract class AuditingEntityId : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}