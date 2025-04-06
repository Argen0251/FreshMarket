package com.example.freshmarket.view.util

import android.content.Context
import com.example.freshmarket.data.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefsHelper {
    private const val PREFS_NAME = "com.example.freshmarket.prefs"
    private const val KEY_FAVORITES = "favorites"

    fun saveFavorites(context: Context, favorites: List<Product>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    fun getFavorites(context: Context): List<Product> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_FAVORITES, null)
        return if (json != null) {
            val type = object : TypeToken<List<Product>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }
}
