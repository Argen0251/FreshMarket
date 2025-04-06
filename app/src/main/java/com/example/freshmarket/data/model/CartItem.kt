package com.example.freshmarket.data.model

data class CartItem(
    val product: Product,
    var quantity: Double = 1.0,      // для товаров, которые продаются на вес (кг)
    val isByWeight: Boolean = true  // true = продаётся на вес (кг), false = штуками
)
