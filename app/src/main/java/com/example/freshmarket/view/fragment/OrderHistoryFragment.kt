package com.example.freshmarket.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freshmarket.R
import com.example.freshmarket.databinding.FragmentOrderHistoryBinding
import com.example.freshmarket.view.main.OrderHistoryAdapter
import com.example.freshmarket.viewmodel.OrderHistoryViewModel

class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    // Используем общий экземпляр OrderHistoryViewModel
    private val viewModel: OrderHistoryViewModel by activityViewModels()
    private lateinit var adapter: OrderHistoryAdapter

    private var allOrders = listOf<com.example.freshmarket.data.model.Order>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderHistoryAdapter(
            emptyList(),
            onItemClick = { order ->
                // Если заказ активен (статус содержит "Оформлен" или "Курьер")
                if (order.status.contains("Оформлен", ignoreCase = true) ||
                    order.status.contains("Курьер", ignoreCase = true)
                ) {
                    val bundle = bundleOf("orderId" to order.id)
                    findNavController().navigate(R.id.orderStatusFragment, bundle)
                } else {
                    Toast.makeText(requireContext(), "Заказ завершён", Toast.LENGTH_SHORT).show()
                }
            },
            onLongClick = { order ->
                // При долгом нажатии показываем диалог подтверждения удаления
                AlertDialog.Builder(requireContext())
                    .setTitle("Удалить заказ")
                    .setMessage("Вы уверены, что хотите удалить заказ №${order.id}?")
                    .setPositiveButton("Да") { _, _ ->
                        viewModel.removeOrder(order.id)
                    }
                    .setNegativeButton("Нет", null)
                    .show()
            }
        )
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter

        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            allOrders = orders
            adapter.updateOrders(allOrders)
        }

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filter = parent?.getItemAtPosition(position).toString()
                if (filter.equals("Все", ignoreCase = true)) {
                    adapter.updateOrders(allOrders)
                } else {
                    val filtered = allOrders.filter { it.status.equals(filter, ignoreCase = true) }
                    adapter.updateOrders(filtered)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapter.updateOrders(allOrders)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
