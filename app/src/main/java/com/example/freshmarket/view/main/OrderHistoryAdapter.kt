package com.example.freshmarket.view.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.freshmarket.data.model.Order
import com.example.freshmarket.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(
    private var orders: List<Order>,
    private val onItemClick: (Order) -> Unit,
    private val onLongClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvOrderId.text = "Заказ №${order.id}"
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            binding.tvOrderDate.text = sdf.format(order.date)
            binding.tvOrderTotal.text = "Сумма: ${order.total} сом"
            binding.tvOrderStatus.text = "Статус: ${order.status}"

            // Устанавливаем фон в зависимости от статуса заказа
            if (order.status.contains("Оформлен", ignoreCase = true) ||
                order.status.contains("Курьер", ignoreCase = true)) {
                binding.root.setBackgroundColor(Color.GREEN)
            } else {
                binding.root.setBackgroundColor(Color.WHITE)
            }

            // Обработка одиночного нажатия – вызываем обработчик
            binding.root.setOnClickListener {
                onItemClick(order)
            }
            // Обработка долгого нажатия для удаления
            binding.root.setOnLongClickListener {
                onLongClick(order)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
