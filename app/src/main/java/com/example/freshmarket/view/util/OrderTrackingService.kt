package com.example.freshmarket.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.freshmarket.R
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext
import kotlin.math.*

class OrderTrackingService : Service() {

    companion object {
        const val TAG = "OrderTrackingService"
        const val CHANNEL_ID = "order_tracking_channel"
        const val ACTION_PROGRESS_UPDATE = "com.example.freshmarket.ORDER_PROGRESS_UPDATE"
        const val EXTRA_ORDER_ID = "orderId"
        const val EXTRA_TIME_LEFT = "timeLeft"
        const val EXTRA_LAT = "lat"
        const val EXTRA_LNG = "lng"
        // Параметры, передаваемые в сервис:
        const val EXTRA_TOTAL_TIME = "totalTimeSec"
        const val EXTRA_ROUTE_POINTS = "routePoints"
    }

    private var orderId: String = ""
    private var totalTimeSec: Int = 0
    private var routePoints: List<LatLng> = emptyList()
    private var startTime: Long = 0L

    // Параметры анимации
    private val stepsPerSegment = 50
    private var currentSegmentIndex = 0
    private var stepInSegment = 0
    private val speed = 2.0  // м/с

    private var serviceJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        orderId = intent?.getStringExtra(EXTRA_ORDER_ID) ?: ""
        totalTimeSec = intent?.getIntExtra(EXTRA_TOTAL_TIME, 0) ?: 0
        // Передаем список маршрута как ArrayList (LatLng реализует Parcelable)
        routePoints = intent?.getParcelableArrayListExtra(EXTRA_ROUTE_POINTS) ?: emptyList()

        if (orderId.isEmpty() || routePoints.isEmpty() || totalTimeSec <= 0) {
            Log.e(TAG, "Invalid parameters, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        startTime = System.currentTimeMillis()

        startForeground(1, buildNotification("Заказ активен"))

        serviceJob = CoroutineScope(Dispatchers.Default).launch {
            runTrackingLoop()
        }

        return START_STICKY
    }

    private suspend fun runTrackingLoop() {
        while (coroutineContext.isActive) {  // Используем coroutineContext.isActive
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            val timeLeft = (totalTimeSec - elapsedSec).coerceAtLeast(0)

            calculateProgress(elapsedSec)
            val currentPosition = calculateCurrentPosition()

            sendProgressUpdate(timeLeft, currentPosition)
            updateNotification("Осталось: $timeLeft сек")

            if (timeLeft <= 0) {
                sendProgressUpdate(0, routePoints.last())
                stopSelf()
                break
            }
            delay(1000)
        }
    }


    private fun calculateProgress(elapsedSec: Int) {
        var accumulatedTime = 0.0
        var found = false
        for (i in 0 until routePoints.size - 1) {
            val segDistance = haversineDistance(routePoints[i], routePoints[i + 1])
            val segTime = segDistance / speed
            if (accumulatedTime + segTime > elapsedSec) {
                currentSegmentIndex = i
                val timeInSegment = elapsedSec - accumulatedTime
                stepInSegment = ((timeInSegment / segTime) * stepsPerSegment).toInt().coerceAtMost(stepsPerSegment)
                found = true
                break
            } else {
                accumulatedTime += segTime
            }
        }
        if (!found) {
            currentSegmentIndex = routePoints.size - 2
            stepInSegment = stepsPerSegment
        }
    }

    private fun calculateCurrentPosition(): LatLng {
        val startPoint = routePoints[currentSegmentIndex]
        val endPoint = routePoints[currentSegmentIndex + 1]
        val fraction = stepInSegment.toDouble() / stepsPerSegment
        return LatLng(
            startPoint.latitude + fraction * (endPoint.latitude - startPoint.latitude),
            startPoint.longitude + fraction * (endPoint.longitude - startPoint.longitude)
        )
    }

    private fun sendProgressUpdate(timeLeft: Int, position: LatLng) {
        val updateIntent = Intent(ACTION_PROGRESS_UPDATE).apply {
            putExtra(EXTRA_ORDER_ID, orderId)
            putExtra(EXTRA_TIME_LEFT, timeLeft)
            putExtra(EXTRA_LAT, position.latitude)
            putExtra(EXTRA_LNG, position.longitude)
        }
        sendBroadcast(updateIntent)
    }

    private fun haversineDistance(a: LatLng, b: LatLng): Double {
        val R = 6371000.0
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val sinLat = sin(dLat / 2)
        val sinLon = sin(dLon / 2)
        val c = 2 * asin(sqrt(sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon))
        return R * c
    }

    private fun buildNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Отслеживание заказа")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_fr_mar) // используйте вашу иконку
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notification = buildNotification(contentText)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Отслеживание заказа",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Уведомления о прогрессе заказа"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
