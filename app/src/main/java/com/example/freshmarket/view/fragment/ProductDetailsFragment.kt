package com.example.freshmarket.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.freshmarket.R
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.data.model.Review
import com.example.freshmarket.databinding.FragmentProductDetailsBinding
import com.example.freshmarket.view.main.ReviewAdapter
import com.example.freshmarket.viewmodel.CartViewModel
import com.example.freshmarket.viewmodel.FavoriteViewModel
import com.example.freshmarket.viewmodel.ReviewViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

class ProductDetailsFragment : Fragment() {

    private val args: ProductDetailsFragmentArgs by navArgs()

    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by viewModels({ requireActivity() })
    private val favoriteViewModel: FavoriteViewModel by viewModels({ requireActivity() })
    private val reviewViewModel: ReviewViewModel by viewModels()

    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Инициализация
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Заполняем информацию о товаре
        binding.tvDetailName.text = args.productName
        binding.tvDetailPrice.text = "Цена: ${args.productPrice}"
        binding.tvDetailDescription.text = args.productDescription

        Glide.with(this)
            .load(args.productImageUrl)
            .placeholder(R.drawable.ic_load)
            .error(R.drawable.ic_fr_mar)
            .into(binding.ivDetailImage)

        // 2) Кнопки +/– (шаг 0.2 кг)
        val etQty = binding.etQuantity
        binding.btnPlus.setOnClickListener {
            val current = etQty.text.toString().toDoubleOrNull() ?: 1.0
            val newVal = current + 0.2
            etQty.setText(String.format("%.1f", newVal))
        }
        binding.btnMinus.setOnClickListener {
            val current = etQty.text.toString().toDoubleOrNull() ?: 1.0
            val newVal = (current - 0.2).coerceAtLeast(0.0)
            etQty.setText(String.format("%.1f", newVal))
        }

        // 3) Добавление в корзину
        binding.btnDetailAddToCart.setOnClickListener {
            val qty = etQty.text.toString().toDoubleOrNull() ?: 1.0
            val product = Product(
                id = args.productId,
                name = args.productName,
                description = args.productDescription,
                priceCents = args.productPrice,
                imageUrl = args.productImageUrl
            )
            cartViewModel.addOrIncrease(product, qty)
            Toast.makeText(requireContext(), "Добавлено $qty кг в корзину!", Toast.LENGTH_SHORT).show()
        }

        // 4) Добавление в избранное
        binding.btnFavorite.setOnClickListener {
            val product = Product(
                id = args.productId,
                name = args.productName,
                description = args.productDescription,
                priceCents = args.productPrice,
                imageUrl = args.productImageUrl
            )
            favoriteViewModel.toggleFavorite(product)
            if (favoriteViewModel.isFavorite(product)) {
                Toast.makeText(requireContext(), "Добавлено в избранное", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show()
            }
        }

        // 5) Подготовка списка отзывов
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        reviewAdapter = ReviewAdapter(
            items = emptyList(),
            currentUserId = currentUserId,
            onDeleteClick = { review ->
                // Удаляем отзыв → Firestore
                reviewViewModel.deleteReview(getFirestoreProductId(), review.reviewId)
            }
        )
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = reviewAdapter

        // Подписываемся на LiveData отзывов
        reviewViewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            reviewAdapter.updateData(reviews)
            val avg = reviewViewModel.getAverageRating()
            binding.tvAvgRating.text = String.format("Средняя оценка: %.1f", avg)
            binding.ratingBarAvg.rating = avg.toFloat()
        }

        // 6) Загружаем отзывы из Firestore для нужного документа
        reviewViewModel.loadReviews(getFirestoreProductId())

        Log.d("ProductDetailsFragment", "Loading reviews for productId: ${args.productId} (Firestore doc = ${getFirestoreProductId()})")

        // 7) Кнопка "Оставить отзыв"
        binding.btnShowReviewDialog.setOnClickListener {
            showReviewDialog()
        }
    }

    /**
     * Если Firestore использует схему product_1, product_2 и т.д.,
     * а в аргументах у вас просто "1", "2", — приводим:
     */
    private fun getFirestoreProductId(): String {
        // Если документ в Firestore называется "product_1"
        // при args.productId="1" вернём "product_1"
        return "product_${args.productId}"
    }

    private fun showReviewDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etReviewText = dialogView.findViewById<EditText>(R.id.etReviewText)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Только авторизованные пользователи могут оставлять отзывы", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUser.uid
        val userEmail = currentUser.email ?: "NoEmail"

        AlertDialog.Builder(requireContext())
            .setTitle("Оставить отзыв")
            .setView(dialogView)
            .setPositiveButton("Отправить") { _, _ ->
                val ratingVal = ratingBar.rating.toDouble()
                val textVal = etReviewText.text.toString().trim()
                if (ratingVal <= 0.0) {
                    Toast.makeText(requireContext(), "Укажите оценку", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val review = Review(
                    userId = userId,
                    userName = userEmail,
                    rating = ratingVal,
                    text = textVal,
                    timestamp = Date(),
                    reviewId = ""
                )
                // сохраняем отзыв в Firestore
                reviewViewModel.addReview(getFirestoreProductId(), review)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
