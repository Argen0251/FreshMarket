package com.example.freshmarket.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

class OrderStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val _routePoints = MutableLiveData<List<LatLng>>(emptyList())
    val routePoints: LiveData<List<LatLng>> get() = _routePoints

    private val _timeLeft = MutableLiveData<Int>(0)
    val timeLeft: LiveData<Int> get() = _timeLeft

    private val _isAnimationRunning = MutableLiveData<Boolean>(false)
    val isAnimationRunning: LiveData<Boolean> get() = _isAnimationRunning

    private val _courierPosition = MutableLiveData<LatLng?>()
    val courierPosition: LiveData<LatLng?> get() = _courierPosition

    private var _isInitialized = false
    val isInitialized: Boolean get() = _isInitialized

    // Сохраняем orderId текущего заказа
    private var currentOrderId: String? = null

    private var currentSegmentIndex = 0
    private var stepInSegment = 0
    private val stepsPerSegment = 50

    // SharedPreferences для сохранения времени запуска заказа
    private val prefs = getApplication<Application>().getSharedPreferences("order_timer", Context.MODE_PRIVATE)

    /**
     * Если приходит новый orderId, сбрасываем состояние.
     */
    fun resetIfNewOrder(orderId: String) {
        if (currentOrderId != orderId) {
            resetState()
            currentOrderId = orderId
        }
    }

    /**
     * Вызывается при получении маршрута.
     * totalTimeSec – общее время для прохождения маршрута.
     * orderId – идентификатор заказа.
     */
    fun setRoutePoints(points: List<LatLng>, totalTimeSec: Int, orderId: String) {
        resetIfNewOrder(orderId)
        if (!_isInitialized) {
            _routePoints.value = points
            val currentTime = System.currentTimeMillis()
            // Пытаемся восстановить сохранённое время старта для данного заказа
            val savedStartTime = prefs.getLong("start_time_$orderId", 0)
            val elapsedSec = if (savedStartTime != 0L) {
                ((currentTime - savedStartTime) / 1000).toInt()
            } else {
                prefs.edit().putLong("start_time_$orderId", currentTime).apply()
                0
            }
            _timeLeft.value = (totalTimeSec - elapsedSec).coerceAtLeast(0)

            // Рассчитываем прогресс по маршруту на основе прошедшего времени
            // Предположим, скорость равна 2 м/с для более длительного отсчёта
            val speed = 2.0 // м/с
            var accumulatedTime = 0.0
            var found = false
            for (i in 0 until points.size - 1) {
                val segDistance = haversineDistance(points[i], points[i + 1])
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
                currentSegmentIndex = points.size - 2
                stepInSegment = stepsPerSegment
            }
            val startPoint = points[currentSegmentIndex]
            val endPoint = points[currentSegmentIndex + 1]
            val fraction = stepInSegment.toDouble() / stepsPerSegment
            _courierPosition.value = LatLng(
                startPoint.latitude + fraction * (endPoint.latitude - startPoint.latitude),
                startPoint.longitude + fraction * (endPoint.longitude - startPoint.longitude)
            )
            _isInitialized = true
        }
    }

    fun startAnimation() {
        if (_isAnimationRunning.value == true) return

        _isAnimationRunning.value = true

        viewModelScope.launch {
            val points = _routePoints.value ?: return@launch
            while (currentSegmentIndex < points.lastIndex && _isAnimationRunning.value == true) {
                animateCurrentSegment(points)
                currentSegmentIndex++
                stepInSegment = 0
            }
            _isAnimationRunning.value = false
        }

        viewModelScope.launch {
            while (_isAnimationRunning.value == true && (_timeLeft.value ?: 0) > 0) {
                delay(1000)
                _timeLeft.value = _timeLeft.value?.minus(1)
            }
        }
    }

    fun stopAnimation() {
        _isAnimationRunning.value = false
    }

    fun resetState() {
        _isInitialized = false
        _routePoints.value = emptyList()
        _timeLeft.value = 0
        _isAnimationRunning.value = false
        _courierPosition.value = null
        currentSegmentIndex = 0
        stepInSegment = 0
    }

    private suspend fun animateCurrentSegment(points: List<LatLng>) {
        if (points.size < 2) return
        val i = currentSegmentIndex
        if (i >= points.lastIndex) return

        val startPoint = points[i]
        val endPoint = points[i + 1]

        val distance = haversineDistance(startPoint, endPoint)
        // Используем ту же скорость, что и при расчёте времени
        val speed = 2.0
        val segTime = distance / speed
        val stepDuration = segTime / stepsPerSegment.toDouble()

        for (step in stepInSegment..stepsPerSegment) {
            if (_isAnimationRunning.value == false) break
            val fraction = step.toDouble() / stepsPerSegment
            val lat = startPoint.latitude + fraction * (endPoint.latitude - startPoint.latitude)
            val lng = startPoint.longitude + fraction * (endPoint.longitude - startPoint.longitude)
            _courierPosition.value = LatLng(lat, lng)
            delay((stepDuration * 1000).toLong())
            stepInSegment++
        }
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
}
