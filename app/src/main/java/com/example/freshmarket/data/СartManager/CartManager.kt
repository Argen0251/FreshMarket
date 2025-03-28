package com.example.freshmarket.data

import com.example.freshmarket.data.model.Product

// Упрощённо: хранит список продуктов (без количества).
// Если нужно учитывать количество - можно сделать data class CartItem(product, quantity).
object CartManager {
    private val cartItems = mutableListOf<Product>()

    fun addToCart(product: Product) {
        cartItems.add(product)
    }

    fun removeFromCart(product: Product) {
        cartItems.remove(product)
    }

    fun getCartItems(): List<Product> {
        return cartItems
    }

    fun clearCart() {
        cartItems.clear()
    }
}
