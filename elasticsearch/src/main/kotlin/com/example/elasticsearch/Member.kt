package com.example.elasticsearch

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

@Document(indexName = "member")
class Member(
    @Id
    val id: Long,
    val name: String,
    val email: String
) {
}

interface MemberRepository : ElasticsearchRepository<Member, Long>