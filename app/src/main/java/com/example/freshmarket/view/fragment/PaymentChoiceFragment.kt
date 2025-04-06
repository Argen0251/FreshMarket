package com.example.freshmarket.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.freshmarket.R
import com.example.freshmarket.data.model.Order
import com.example.freshmarket.databinding.FragmentPaymentChoiceBinding
import com.example.freshmarket.viewmodel.CartViewModel
import com.example.freshmarket.viewmodel.OrderHistoryViewModel
import kotlinx.coroutines.*
import java.util.Date

class PaymentChoiceFragment : Fragment() {

    private var _binding: FragmentPaymentChoiceBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private val orderHistoryViewModel: OrderHistoryViewModel by activityViewModels()

    private var paymentJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirmPaymentMethod.setOnClickListener {
            val selectedId = binding.rgPaymentMethod.checkedRadioButtonId
            if (selectedId == -1) return@setOnClickListener

            // Проверяем, есть ли уже активный заказ
            val activeOrderExists = orderHistoryViewModel.orders.value?.any {
                it.status.contains("Оформлен", ignoreCase = true) ||
                        it.status.contains("Курьер", ignoreCase = true)
            } ?: false

            if (activeOrderExists) {
                Toast.makeText(requireContext(), "У вас уже есть активный заказ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val radioButton = binding.rgPaymentMethod.findViewById<RadioButton>(selectedId)
            val methodName = radioButton.text.toString()

            if (methodName.contains("Налич")) {
                createOrder("Наличные")
            } else {
                findNavController().navigate(R.id.action_paymentChoiceFragment_to_paymentCardFragment)
            }
        }

    }

    private fun createOrder(paymentMethod: String) {
        paymentJob = CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            val total = cartViewModel.totalPrice.value ?: 0
            // Получаем последовательный номер заказа
            val orderId = orderHistoryViewModel.getNextOrderId()
            val newOrder = Order(
                id = orderId,
                date = Date(),
                total = total,
                status = "Оформлен ($paymentMethod)"
            )
            orderHistoryViewModel.addOrder(newOrder)
            cartViewModel.clearCart()
            Log.d("PaymentChoiceFrag", "Создан заказ с id: $orderId")
            val bundle = bundleOf("orderId" to orderId)
            findNavController().navigate(R.id.orderStatusFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        paymentJob?.cancel()
        _binding = null
    }
}
