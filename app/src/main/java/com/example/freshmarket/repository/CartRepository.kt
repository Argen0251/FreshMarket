package com.example.freshmarket.repository

import com.example.freshmarket.data.model.Product

class CartRepository {

    private val cartItems = mutableListOf<Product>()

    fun getCartItems(): List<Product> {
        return cartItems
    }

    fun addToCart(product: Product) {
        cartItems.add(product)
    }

    fun removeFromCart(product: Product) {
        cartItems.remove(product)
    }

    fun clearCart() {
        cartItems.clear()
    }
}
