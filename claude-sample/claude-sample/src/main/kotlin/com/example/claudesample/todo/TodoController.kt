package com.example.claudesample.todo

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
class TodoController(
    private val todoService: TodoService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: TodoCreateRequest): TodoResponse {
        return todoService.create(request)
    }

    @GetMapping
    fun findAll(): List<TodoResponse> {
        return todoService.findAll()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): TodoResponse {
        return todoService.findById(id)
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: TodoUpdateRequest,
    ): TodoResponse {
        return todoService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        todoService.delete(id)
    }
}
