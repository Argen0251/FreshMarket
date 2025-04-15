package com.example.freshmarket.repository

import android.util.Log
import com.example.freshmarket.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Добавляем отзыв. Возвращаем сгенерированный reviewId.
     */
    suspend fun addReview(productId: String, review: Review): String {
        return try {
            val reviewsColl = firestore.collection("products")
                .document(productId)
                .collection("reviews")

            // Генерируем auto-id
            val newDocRef = reviewsColl.document()
            val docId = newDocRef.id

            // Запишем docId в review, чтобы потом уметь удалять
            val updatedReview = review.copy(reviewId = docId)

            newDocRef.set(updatedReview).await()
            Log.d("ReviewRepository", "Review added successfully to $productId with id=$docId")
            docId
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Ошибка при добавлении отзыва: ${e.message}")
            throw e
        }
    }

    /**
     * Получаем список всех отзывов для товара productId
     */
    suspend fun getReviews(productId: String): List<Review> {
        return try {
            val reviewsSnap = firestore.collection("products")
                .document(productId)
                .collection("reviews")
                .get()
                .await()

            reviewsSnap.documents.mapNotNull { doc ->
                // Преобразуем документ -> Review
                doc.toObject(Review::class.java)
                    ?.copy(reviewId = doc.id) // на всякий случай ещё раз укажем reviewId
            }
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Ошибка при загрузке отзывов: ${e.message}")
            emptyList()
        }
    }

    /**
     * Удаляем конкретный отзыв. Проверку на совпадение userId снаружи делаем.
     */
    suspend fun deleteReview(productId: String, reviewId: String): Int {
        return try {
            firestore.collection("products")
                .document(productId)
                .collection("reviews")
                .document(reviewId)
                .delete()
                .await()
            Log.d("ReviewRepository", "Отзыв $reviewId удалён из $productId")
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Ошибка при удалении отзыва: ${e.message}")
            throw e
        }
    }
}
