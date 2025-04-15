package com.example.freshmarket.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.repository.FavoriteRepository

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteRepository = FavoriteRepository(application.applicationContext)

    private val _favoriteItems = MutableLiveData<List<Product>>(favoriteRepository.getFavoriteItems())
    val favoriteItems: LiveData<List<Product>> = _favoriteItems

    fun addToFavorites(product: Product) {
        favoriteRepository.addToFavorite(product)
        updateFavorites()
    }

    fun removeFromFavorites(product: Product) {
        favoriteRepository.removeFromFavorite(product)
        updateFavorites()
    }

    fun toggleFavorite(product: Product) {
        if (favoriteRepository.isFavorite(product)) {
            favoriteRepository.removeFromFavorite(product)
        } else {
            favoriteRepository.addToFavorite(product)
        }
        updateFavorites()
    }

    fun isFavorite(product: Product): Boolean {
        return favoriteRepository.isFavorite(product)
    }

    private fun updateFavorites() {
        _favoriteItems.value = favoriteRepository.getFavoriteItems()
    }
}
