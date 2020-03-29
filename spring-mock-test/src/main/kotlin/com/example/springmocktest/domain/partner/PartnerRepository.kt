package com.example.springmocktest.domain.partner

import org.springframework.data.jpa.repository.JpaRepository

interface PartnerRepository: JpaRepository<Partner, Long> {
}