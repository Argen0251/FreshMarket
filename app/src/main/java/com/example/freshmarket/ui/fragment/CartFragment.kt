package com.example.freshmarket.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.freshmarket.R
import com.example.freshmarket.data.CartManager
import com.example.freshmarket.databinding.FragmentCartBinding
import com.example.freshmarket.ui.GridSpacingItemDecoration
import com.example.freshmarket.ui.main.CartAdapter

class CartFragment : Fragment() {

    // Используем backing property для binding
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private var cartAdapter: CartAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Инициализация логики происходит в onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        updateTotalPrice()

        binding.btnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_nav_cart_to_checkoutFragment)
        }
    }

    private fun setupRecyclerView() {
        binding.rvCartItems.layoutManager = GridLayoutManager(requireContext(), 2)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.item_inner_spacing)
        binding.rvCartItems.addItemDecoration(GridSpacingItemDecoration(spacingPx))

        val cartItems = CartManager.getCartItems()
        cartAdapter = CartAdapter(
            cartItems,
            onRemoveClick = { product ->
                CartManager.removeFromCart(product)
                updateCartList()
            }
        )
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun updateCartList() {
        cartAdapter?.notifyDataSetChanged()
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val totalCents = CartManager.getCartItems().sumOf { it.priceCents }
        val totalStr = if (totalCents % 1.0 == 0.0) {
            "${totalCents.toInt()} сом"
        } else {
            String.format("%.1f сом", totalCents)
        }
        binding.tvTotalPrice.text = "Сумма: $totalStr"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
