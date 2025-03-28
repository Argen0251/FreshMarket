package com.example.freshmarket.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.freshmarket.R
import com.example.freshmarket.data.CartManager
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.databinding.FragmentProductDetailsBinding

class ProductDetailsFragment : Fragment() {

    private val args: ProductDetailsFragmentArgs by navArgs()

    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Заполняем поля данными из args:
        binding.tvDetailName.text = args.productName
        binding.tvDetailPrice.text = "Цена: ${args.productPrice}"
        binding.tvDetailDescription.text = args.productDescription

        Glide.with(this)
            .load(args.productImageUrl)
            .placeholder(R.drawable.ic_load)
            .error(R.drawable.ic_fr_mar)
            .into(binding.ivDetailImage)

        binding.btnDetailAddToCart.setOnClickListener {
            // Создаём объект продукта на основе полученных данных:
            val product = Product(
                id = args.productId,
                name = args.productName,
                description = args.productDescription,
                priceCents = args.productPrice,
                imageUrl = args.productImageUrl
            )
            CartManager.addToCart(product)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
