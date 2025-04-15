package com.example.freshmarket.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshmarket.data.model.Review
import com.example.freshmarket.repository.ReviewRepository
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {

    private val repository = ReviewRepository()

    // Храним список отзывов
    private val _reviews = MutableLiveData<List<Review>>(emptyList())
    val reviews: LiveData<List<Review>> = _reviews

    /**
     * Подгружаем все отзывы для данного productId
     */
    fun loadReviews(productId: String) {
        viewModelScope.launch {
            try {
                val list = repository.getReviews(productId)
                _reviews.value = list
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Ошибка при загрузке отзывов: ${e.message}")
            }
        }
    }

    /**
     * Добавляем отзыв. Затем перезагружаем список.
     */
    fun addReview(productId: String, review: Review) {
        viewModelScope.launch {
            try {
                repository.addReview(productId, review)
                loadReviews(productId)
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Ошибка при добавлении отзыва: ${e.message}")
            }
        }
    }

    /**
     * Удаляем отзыв (если userId совпадает)
     */
    fun deleteReview(productId: String, reviewId: String) {
        viewModelScope.launch {
            try {
                repository.deleteReview(productId, reviewId)
                loadReviews(productId)
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Ошибка при удалении отзыва: ${e.message}")
            }
        }
    }

    /**
     * Считаем средний рейтинг из текущего списка
     */
    fun getAverageRating(): Double {
        val list = _reviews.value ?: return 0.0
        if (list.isEmpty()) return 0.0
        return list.map { it.rating }.average()
    }
}
