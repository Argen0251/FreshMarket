package com.example.freshmarket.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.freshmarket.data.model.Review
import com.example.freshmarket.databinding.ItemReviewBinding

class ReviewAdapter(
    private var items: List<Review>,
    private val currentUserId: String,             // uid текущего пользователя
    private val onDeleteClick: (Review) -> Unit    // коллбэк на удаление
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvUserName.text = review.userName
            binding.tvRating.text = "Оценка: ${review.rating}"
            binding.tvReviewText.text = review.text

            // Если это наш отзыв, делаем кнопку "Удалить" видимой
            if (review.userId == currentUserId) {
                binding.btnDeleteReview.visibility = View.VISIBLE
                binding.btnDeleteReview.setOnClickListener {
                    onDeleteClick(review)
                }
            } else {
                binding.btnDeleteReview.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Review>) {
        items = newItems
        notifyDataSetChanged()
    }
}
