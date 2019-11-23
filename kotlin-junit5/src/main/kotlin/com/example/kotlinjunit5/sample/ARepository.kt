package com.example.kotlinjunit5.sample

import org.springframework.data.jpa.repository.JpaRepository

interface ARepository : JpaRepository<A, Long> {
}