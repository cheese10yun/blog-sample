package com.example.kotlincoroutine

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
class ReDocController {

    @GetMapping("/redoc")
    fun redoc(): String {
        return "redirect:/redoc.html"
    }
}