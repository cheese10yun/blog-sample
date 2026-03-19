package com.example.claudesample.todo

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TodoService(
    private val todoRepository: TodoRepository,
) {

    @Transactional
    fun create(request: TodoCreateRequest): TodoResponse {
        val todo = Todo(
            title = request.title,
            description = request.description,
        )
        return TodoResponse.from(todoRepository.save(todo))
    }

    fun findAll(): List<TodoResponse> {
        return todoRepository.findAll().map { TodoResponse.from(it) }
    }

    fun findById(id: Long): TodoResponse {
        val todo = todoRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Todo not found. id=$id")
        return TodoResponse.from(todo)
    }

    @Transactional
    fun update(id: Long, request: TodoUpdateRequest): TodoResponse {
        val todo = todoRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Todo not found. id=$id")
        request.title?.let { todo.title = it }
        request.description?.let { todo.description = it }
        request.isDone?.let { todo.isDone = it }
        return TodoResponse.from(todo)
    }

    @Transactional
    fun delete(id: Long) {
        if (!todoRepository.existsById(id)) {
            throw NoSuchElementException("Todo not found. id=$id")
        }
        todoRepository.deleteById(id)
    }
}
