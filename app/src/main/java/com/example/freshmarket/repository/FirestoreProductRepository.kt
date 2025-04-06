package com.example.freshmarket.repository

import android.util.Log
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreProductRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")

    // Получаем все продукты из Firestore
    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = productsCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
        } catch (e: Exception) {
            Log.e("FirestoreProductRepo", "Ошибка при загрузке продуктов: ${e.message}")
            emptyList()
        }
    }

    // Получаем продукты по заданной категории
    suspend fun getProductsByCategory(categoryName: String): List<Product> {
        return try {
            val snapshot = productsCollection.whereEqualTo("category", categoryName).get().await()
            snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
        } catch (e: Exception) {
            Log.e("FirestoreProductRepo", "Ошибка получения продуктов по категории: ${e.message}")
            emptyList()
        }
    }

    // Получаем список категорий из коллекции "category" в Firestore
    suspend fun getAllCategories(): List<Category> {
        return try {
            val snapshot = firestore.collection("category").get().await()
            snapshot.documents.mapNotNull { it.toObject(Category::class.java) }
        } catch (e: Exception) {
            Log.e("FirestoreProductRepo", "Ошибка при загрузке категорий: ${e.message}")
            emptyList()
        }
    }

    // Метод для добавления продукта (если нужен)
    suspend fun addProduct(product: Product) {
        try {
            val newDocRef = if (product.id.isEmpty()) {
                productsCollection.document()
            } else {
                productsCollection.document(product.id)
            }
            val newId = newDocRef.id
            val productWithId = product.copy(id = newId)
            newDocRef.set(productWithId).await()
        } catch (e: Exception) {
            Log.e("FirestoreProductRepo", "Ошибка при добавлении продукта: ${e.message}")
        }
    }
}
