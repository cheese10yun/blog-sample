//package com.example.boot3mongo.dbref
//
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/posts")
//class PostController(
//    private val aggregationService : AggregationService
//) {
//
//    @GetMapping
//    fun getPosts(): List<Post> {
//        return aggregationService.get()
//    }
//}
//
//@Service
//class AggregationService(
//    private val postRepository: PostRepository,
//    private val paymentRepository: PaymentRepository
//){
//
//    @Transactional
//    fun get(): MutableList<Post> {
//        paymentRepository.findAll()
//        return postRepository.findAll()
//    }
//}