package com.example.freshmarket.repository

import android.content.Context
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.view.util.SharedPrefsHelper

class FavoriteRepository(private val context: Context) {

    // Загружаем сохранённые избранные товары при создании репозитория
    private val favoriteItems = mutableListOf<Product>().apply {
        addAll(SharedPrefsHelper.getFavorites(context))
    }

    fun getFavoriteItems(): List<Product> = favoriteItems

    fun addToFavorite(product: Product) {
        if (!favoriteItems.contains(product)) {
            favoriteItems.add(product)
            SharedPrefsHelper.saveFavorites(context, favoriteItems)
        }
    }

    fun removeFromFavorite(product: Product) {
        favoriteItems.remove(product)
        SharedPrefsHelper.saveFavorites(context, favoriteItems)
    }

    fun clearFavorites() {
        favoriteItems.clear()
        SharedPrefsHelper.saveFavorites(context, favoriteItems)
    }

    fun isFavorite(product: Product): Boolean = favoriteItems.contains(product)
}
