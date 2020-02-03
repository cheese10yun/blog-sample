package com.example.batch.common

data class PageResponse<T>(
    val totalPages: Int,
    val totalElements: Int,
    val last: Boolean,
    val first: Boolean,
    val size: Int,
    val number: Int,
    val empty: Boolean,
    var content: MutableList<T>
)