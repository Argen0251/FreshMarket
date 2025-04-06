package com.example.freshmarket.viewmodel

import android.app.Application
import android.content.Context
import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.freshmarket.R
import com.example.freshmarket.data.model.Order
import com.example.freshmarket.repository.PersistentOrderHistoryRepository
import androidx.navigation.NavDeepLinkBuilder
import com.example.freshmarket.view.main.MainActivity

class OrderHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PersistentOrderHistoryRepository(application.applicationContext)
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> get() = _orders

    // Флаг, чтобы уведомление "Курьер в пути" отправлялось один раз
    private var courierNotificationSent = false

    // Контекст приложения для уведомлений
    private val appContext: Context = application.applicationContext

    init {
        loadOrders()
    }

    private fun loadOrders() {
        _orders.value = repository.getOrders()
    }

    fun addOrder(order: Order) {
        Log.d("OrderHistoryVM", "Добавление заказа ${order.id} со статусом: ${order.status}")
        repository.addOrder(order)
        loadOrders()
        // Сброс флага уведомления для нового заказа
        courierNotificationSent = false
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        Log.d("OrderHistoryVM", "Обновление заказа $orderId на статус: $newStatus")
        val existingOrder = _orders.value?.find { it.id == orderId }
        if (existingOrder == null) {
            Log.d("OrderHistoryVM", "Заказ с id $orderId не найден")
            return
        }
        // Если заказ уже имеет нужный статус – не обновляем
        if (existingOrder.status == newStatus) {
            Log.d("OrderHistoryVM", "Статус заказа уже '$newStatus', обновление не требуется.")
            return
        }
        val updatedOrder = existingOrder.copy(status = newStatus)
        repository.updateOrder(updatedOrder)
        loadOrders()

        // Если статус содержит "Курьер" и уведомление ещё не отправлено, отправляем уведомление
        if (newStatus.contains("Курьер", ignoreCase = true) && !courierNotificationSent) {
            showStatusNotification(updatedOrder)
            courierNotificationSent = true
        }
        // Если статус изменился на "Доставлен", сбрасываем флаг (на случай повторного заказа)
        if (newStatus.contains("Доставлен", ignoreCase = true)) {
            courierNotificationSent = false
        }
    }

    private fun showStatusNotification(order: Order) {
        val channelId = "order_channel_id"
        val notificationId = order.id.hashCode()

        // Создаем Intent с действием TRACK_ORDER и передаем orderId
        val trackIntent = Intent(appContext, MainActivity::class.java).apply {
            action = "com.example.freshmarket.TRACK_ORDER"
            putExtra("orderId", order.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            notificationId,
            trackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_fr_mar)
            .setContentTitle("Заказ оформлен")
            .setContentText("Ваш заказ в пути. Нажмите, чтобы увидеть маршрут.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(appContext).notify(notificationId, builder.build())
            Log.d("OrderHistoryVM", "Уведомление отправлено для заказа ${order.id}")
        } else {
            Log.d("OrderHistoryVM", "Нет разрешения POST_NOTIFICATIONS, уведомление не отправлено.")
        }
    }


    fun removeOrder(orderId: String) {
        Log.d("OrderHistoryVM", "Удаление заказа с id: $orderId")
        repository.removeOrder(orderId)
        loadOrders()
    }

    fun getNextOrderId(): String {
        return repository.getNextOrderNumber()
    }
}
