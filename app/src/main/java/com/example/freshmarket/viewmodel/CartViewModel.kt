package com.example.freshmarket.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.freshmarket.data.model.CartItem
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.repository.CartRepository

class CartViewModel : ViewModel() {

    private val cartRepository = CartRepository()

    // Живое состояние списка CartItem
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    // Общая сумма (сом)
    private val _totalPrice = MutableLiveData<Int>(0)
    val totalPrice: LiveData<Int> get() = _totalPrice

    /**
     * Добавить новый товар или увеличить количество на 1 (или 0.5, если нужно).
     */
    fun addOrIncrease(product: Product, increment: Double = 1.0) {
        cartRepository.addOrIncrease(product, increment)
        updateCartState()
    }

    /**
     * Обновить количество
     */
    fun updateItemQuantity(productId: String, newQuantity: Double) {
        cartRepository.updateQuantity(productId, newQuantity)
        updateCartState()
    }

    /**
     * Удалить товар целиком из корзины
     */
    fun removeItem(productId: String) {
        cartRepository.removeItem(productId)
        updateCartState()
    }

    /**
     * Очистить корзину
     */
    fun clearCart() {
        cartRepository.clearCart()
        updateCartState()
    }

    /**
     * Пересчитать состояние корзины
     */
    private fun updateCartState() {
        val items = cartRepository.getCartItems()
        _cartItems.value = items

        // Предполагаем, что priceCents = цена за 1 кг или за 1 шт.
        // Общую сумму можно считать как sumOf { it.product.priceCents * it.quantity }
        // но priceCents - Int, quantity - Double => результат Double.
        // Если нужно, переводим обратно в Int.
        val totalDouble = items.sumOf { it.product.priceCents * it.quantity }
        // Округлим, если хотим целое:
        _totalPrice.value = totalDouble.toInt()
    }
}
