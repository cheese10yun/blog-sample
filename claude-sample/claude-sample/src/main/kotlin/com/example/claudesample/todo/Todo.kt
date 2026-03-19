package com.example.claudesample.todo

import jakarta.persistence.*

@Entity
@Table(name = "todo")
class Todo(
    @Column(nullable = false)
    var title: String,

    @Column
    var description: String? = null,

    @Column(nullable = false)
    var isDone: Boolean = false,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
)
