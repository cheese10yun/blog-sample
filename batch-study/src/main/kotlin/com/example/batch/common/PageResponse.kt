package com.example.batch.common

data class PageResponse<domainClass: Class<*>>(
    val totalPages: Int,
    val totalElements: Int,
    val last: Boolean,
    val first: Boolean,
    val size: Int,
    val number: Int,
    val empty: Boolean,
    val content: MutableList<domainClass>
)