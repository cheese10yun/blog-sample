package com.example.claudesample.todo

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/todos")
class TodoWebController(
    private val todoService: TodoService,
) {

    @GetMapping("/list")
    fun list(model: Model): String {
        val todos = todoService.findAll()
        model.addAttribute("todos", todos)
        return "todo/list"
    }

    @GetMapping("/create")
    fun createForm(): String {
        return "todo/create"
    }

    @PostMapping("/create")
    fun create(
        @RequestParam title: String,
        @RequestParam(required = false) description: String?,
    ): String {
        todoService.create(TodoCreateRequest(title, description))
        return "redirect:/todos"
    }

    @PostMapping("/{id}/toggle")
    fun toggleDone(@PathVariable id: Long): String {
        val todo = todoService.findById(id)
        todoService.update(id, TodoUpdateRequest(isDone = !todo.isDone))
        return "redirect:/todos"
    }

    @PostMapping("/{id}/delete")
    fun delete(@PathVariable id: Long): String {
        todoService.delete(id)
        return "redirect:/todos"
    }
}
