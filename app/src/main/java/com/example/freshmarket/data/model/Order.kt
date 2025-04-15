package com.example.freshmarket.data.model

import java.util.Date

data class Order(
    val id: String,
    val date: Date,
    val total: Int,
    val status: String
)
