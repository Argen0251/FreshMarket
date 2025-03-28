package com.example.freshmarket.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.freshmarket.data.CartManager
import com.example.freshmarket.databinding.FragmentCheckoutBinding

class CheckoutFragment : Fragment() {

    // Используем backing property для binding
    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Инициализация логики происходит в onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirmOrder.setOnClickListener {
            val name = binding.etName.text.toString()
            val address = binding.etAddress.text.toString()

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля!", Toast.LENGTH_SHORT).show()
            } else {
                // Оформляем заказ: очищаем корзину и показываем сообщение
                CartManager.clearCart()
                Toast.makeText(requireContext(), "Заказ оформлен!", Toast.LENGTH_LONG).show()

                // Возвращаемся на предыдущие фрагменты:
                parentFragmentManager.popBackStack() // закрывает CheckoutFragment
                parentFragmentManager.popBackStack() // закрывает CartFragment
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
