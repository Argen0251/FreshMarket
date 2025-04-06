package com.example.freshmarket.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.freshmarket.R
import com.example.freshmarket.databinding.FragmentCartBinding
import com.example.freshmarket.view.main.CartAdapter
import com.example.freshmarket.view.main.GridSpacingItemDecoration
import com.example.freshmarket.viewmodel.CartViewModel

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()

    private var cartAdapter: CartAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Инициализация
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_nav_cart_to_checkoutFragment)
        }
    }

    private fun setupRecyclerView() {
        binding.rvCartItems.layoutManager = GridLayoutManager(requireContext(), 2)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.item_inner_spacing)
        binding.rvCartItems.addItemDecoration(GridSpacingItemDecoration(spacingPx))

        cartAdapter = CartAdapter(
            items = emptyList(),
            onRemoveClick = { product ->
                cartViewModel.removeFromCart(product)
            }
        )
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun observeViewModel() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter?.updateItems(items)
        }
        cartViewModel.totalPrice.observe(viewLifecycleOwner) { totalCents ->
            val totalStr = if (totalCents % 1 == 0) {
                "${totalCents} сом"
            } else {
                String.format("%.1f сом", totalCents)
            }
            binding.tvTotalPrice.text = "Сумма: $totalStr"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
