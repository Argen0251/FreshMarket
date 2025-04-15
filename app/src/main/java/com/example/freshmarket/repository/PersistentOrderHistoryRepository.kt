    package com.example.freshmarket.repository

    import android.content.Context
    import com.example.freshmarket.data.model.Order
    import com.google.gson.Gson
    import com.google.gson.reflect.TypeToken

    class PersistentOrderHistoryRepository(context: Context) {

        private val sharedPrefs = context.getSharedPreferences("order_history_prefs", Context.MODE_PRIVATE)
        private val gson = Gson()
        private val key = "orders"
        private val counterKey = "order_counter"

        fun getOrders(): List<Order> {
            val json = sharedPrefs.getString(key, null)
            return if (json != null) {
                val type = object : TypeToken<List<Order>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }
        }

        fun addOrder(order: Order) {
            val orders = getOrders().toMutableList()
            orders.add(order)
            saveOrders(orders)
        }

        fun updateOrder(updatedOrder: Order) {
            val orders = getOrders().toMutableList()
            val index = orders.indexOfFirst { it.id == updatedOrder.id }
            if (index != -1) {
                orders[index] = updatedOrder
                saveOrders(orders)
            }
        }

        fun removeOrder(orderId: String) {
            val orders = getOrders().toMutableList()
            if (orders.removeAll { it.id == orderId }) {
                saveOrders(orders)
            }
        }

        // Сделали функцию публичной для использования из Worker
        fun saveOrders(orders: List<Order>) {
            val json = gson.toJson(orders)
            sharedPrefs.edit().putString(key, json).apply()
        }

        fun getNextOrderNumber(): String {
            val currentNumber = sharedPrefs.getInt(counterKey, 0)
            val nextNumber = currentNumber + 1
            sharedPrefs.edit().putInt(counterKey, nextNumber).apply()
            return nextNumber.toString()
        }
    }
