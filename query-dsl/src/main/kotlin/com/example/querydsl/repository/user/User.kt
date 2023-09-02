package com.example.querydsl.repository.user

import com.example.querydsl.domain.EntityAuditing
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table


@Entity
@Table(name = "user")
class User(
    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "age", nullable = false)
    var age: Int = 0
) : EntityAuditing()


interface UserRepository : JpaRepository<User, Long>, UserCustomRepository

interface UserCustomRepository {
    fun find(pageable: Pageable): Page<User>
}

class UserCustomRepositoryImpl : QuerydslRepositorySupport(User::class.java), UserCustomRepository {

    override fun find(pageable: Pageable): Page<User> {
        val query = from(QUser.user)
            .select(QUser.user)

        return PageImpl(querydsl!!.applyPagination(pageable, query).fetch(), pageable, query.fetchCount())


    }
}

@RestController
@RequestMapping("/api/users")
class UserApi(
    private val userRepository: UserRepository
) {

    @GetMapping
    fun getUser(
        @PageableDefault pageable: Pageable
    ): Page<User> {
        return userRepository.find(pageable)
    }
}