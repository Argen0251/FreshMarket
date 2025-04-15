package com.example.freshmarket.view.fragment

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDeepLinkBuilder
import com.example.freshmarket.R
import com.example.freshmarket.data.model.Order
import com.example.freshmarket.databinding.FragmentPaymentCardBinding
import com.example.freshmarket.view.main.MainActivity
import com.example.freshmarket.viewmodel.CartViewModel
import com.example.freshmarket.viewmodel.OrderHistoryViewModel
import kotlinx.coroutines.*
import java.util.Date

class PaymentCardFragment : Fragment() {

    companion object {
        private const val TAG = "PaymentCardFragment"
        private const val NOTIFICATION_REQUEST_CODE = 999
    }

    private var _binding: FragmentPaymentCardBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private val orderHistoryViewModel: OrderHistoryViewModel by activityViewModels()

    private var payJob: Job? = null
    private val notificationChannelId = MainActivity.NOTIFICATION_CHANNEL_ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPayNow.setOnClickListener {
            Log.d(TAG, "btnPayNow clicked")
            binding.btnPayNow.isEnabled = false
            binding.progressBarCardPayment.visibility = View.VISIBLE

            payJob = CoroutineScope(Dispatchers.Main).launch {
                delay(2000) // имитация оплаты
                finishPayment()
            }
        }
    }

    private fun finishPayment() {
        Log.d(TAG, "finishPayment() called")
        val total = cartViewModel.totalPrice.value ?: 0
        val newOrder = Order(
            id = System.currentTimeMillis().toString(),
            date = Date(),
            total = total,
            status = "Оформлен (Картой)"
        )
        orderHistoryViewModel.addOrder(newOrder)
        cartViewModel.clearCart()

        Toast.makeText(requireContext(), "Оплата прошла успешно!", Toast.LENGTH_LONG).show()
        Log.d(TAG, "Создан заказ: ${newOrder.id}")

        checkPermissionAndNotify(newOrder)

        binding.btnPayNow.isEnabled = true
        binding.progressBarCardPayment.visibility = View.GONE
    }

    private fun checkPermissionAndNotify(order: Order) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "Android < 13, отправляем уведомление")
            sendLocalNotification(order)
        } else {
            val permission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Разрешение POST_NOTIFICATIONS уже есть")
                sendLocalNotification(order)
            } else {
                Log.d(TAG, "Запрашиваем разрешение POST_NOTIFICATIONS")
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_REQUEST_CODE)
                tempOrder = order
            }
        }
    }

    private var tempOrder: Order? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Разрешение POST_NOTIFICATIONS получено")
                tempOrder?.let { sendLocalNotification(it) }
            } else {
                Log.d(TAG, "Разрешение POST_NOTIFICATIONS отклонено")
                Toast.makeText(requireContext(), "Уведомления отключены", Toast.LENGTH_SHORT).show()
            }
            tempOrder = null
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun sendLocalNotification(order: Order) {
        // Проверяем, не было ли уже уведомления для этого заказа
        if (NotificationManagerCompat.from(requireContext()).activeNotifications.any { it.id == order.id.hashCode() }) {
            return
        }

        Log.d(TAG, "sendLocalNotification() called for order ${order.id}")
        val deepLinkPendingIntent = NavDeepLinkBuilder(requireContext())
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.orderStatusFragment)
            .setArguments(bundleOf("orderId" to order.id))
            .createPendingIntent()

        val builder = NotificationCompat.Builder(requireContext(), notificationChannelId)
            .setSmallIcon(R.drawable.ic_fr_mar)
            .setContentTitle("Заказ оформлен")
            .setContentText("Ваш заказ в пути. Нажмите, чтобы увидеть маршрут.")
            .setContentIntent(deepLinkPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(requireContext()).notify(order.id.hashCode(), builder.build())
        Log.d(TAG, "notificationManager.notify() вызвано")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        payJob?.cancel()
        _binding = null
    }
}