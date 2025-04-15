package com.example.freshmarket.repository

import com.example.freshmarket.data.model.CartItem
import com.example.freshmarket.data.model.Product

class CartRepository {

    private val cartItems = mutableListOf<CartItem>()

    fun getCartItems(): List<CartItem> = cartItems

    /**
     * Добавить в корзину. Если товар уже есть, увеличить количество.
     */
    fun addOrIncrease(product: Product, increment: Double = 1.0) {
        val existing = cartItems.find { it.product.id == product.id }
        if (existing != null) {
            existing.quantity += increment
        } else {
            cartItems.add(CartItem(product, increment))
        }
    }

    /**
     * Обновить количество для конкретного productId
     */
    fun updateQuantity(productId: String, newQuantity: Double) {
        val existing = cartItems.find { it.product.id == productId } ?: return
        if (newQuantity <= 0.0) {
            cartItems.remove(existing)
        } else {
            existing.quantity = newQuantity
        }
    }

    fun removeItem(productId: String) {
        cartItems.removeAll { it.product.id == productId }
    }

    fun clearCart() {
        cartItems.clear()
    }
}
