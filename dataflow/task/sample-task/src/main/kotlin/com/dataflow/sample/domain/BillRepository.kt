package com.dataflow.sample.domain

import org.springframework.data.jpa.repository.JpaRepository

interface BillRepository: JpaRepository<Bill, Long> {
}