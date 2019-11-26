package com.example.applicationevent.order

data class OrderCompletedEvent(
        val itemCodes: List<String>
)