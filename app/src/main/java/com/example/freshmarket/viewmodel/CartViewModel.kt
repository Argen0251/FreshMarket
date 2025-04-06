package com.example.freshmarket.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.repository.CartRepository

class CartViewModel : ViewModel() {

    private val cartRepository = CartRepository()

    private val _cartItems = MutableLiveData<List<Product>>(emptyList())
    val cartItems: LiveData<List<Product>> get() = _cartItems

    private val _totalPrice = MutableLiveData<Int>(0)
    val totalPrice: LiveData<Int> get() = _totalPrice

    fun addToCart(product: Product) {
        cartRepository.addToCart(product)
        updateCartState()
    }

    fun removeFromCart(product: Product) {
        cartRepository.removeFromCart(product)
        updateCartState()
    }

    fun clearCart() {
        cartRepository.clearCart()
        updateCartState()
    }

    private fun updateCartState() {
        val items = cartRepository.getCartItems()
        _cartItems.value = items
        _totalPrice.value = items.sumOf { it.priceCents }
    }
}
