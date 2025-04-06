package com.example.freshmarket.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.freshmarket.R
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.databinding.FragmentProductDetailsBinding
import com.example.freshmarket.viewmodel.CartViewModel
import com.example.freshmarket.viewmodel.FavoriteViewModel

class ProductDetailsFragment : Fragment() {

    private val args: ProductDetailsFragmentArgs by navArgs()

    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteViewModel by activityViewModels()

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

        binding.tvDetailName.text = args.productName
        binding.tvDetailPrice.text = "Цена: ${args.productPrice}"
        binding.tvDetailDescription.text = args.productDescription

        Glide.with(this)
            .load(args.productImageUrl)
            .placeholder(R.drawable.ic_load)
            .error(R.drawable.ic_fr_mar)
            .into(binding.ivDetailImage)

        // Добавляем товар в корзину
        // ProductDetailsFragment.kt (в методе onViewCreated)
        binding.btnDetailAddToCart.setOnClickListener {
            val product = Product(
                id = args.productId,
                name = args.productName,
                description = args.productDescription,
                priceCents = args.productPrice,
                imageUrl = args.productImageUrl
            )
            cartViewModel.addToCart(product)

            Toast.makeText(requireContext(), "Товар добавлен в корзину!", Toast.LENGTH_SHORT).show()
        }


        // Обработка нажатия кнопки избранного
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
