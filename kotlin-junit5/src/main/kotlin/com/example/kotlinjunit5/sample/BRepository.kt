package com.example.kotlinjunit5.sample

import org.springframework.data.jpa.repository.JpaRepository

interface BRepository : JpaRepository<B, Long> {
}