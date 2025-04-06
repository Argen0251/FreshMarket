package com.example.freshmarket.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.freshmarket.R
import com.example.freshmarket.databinding.FragmentOrderStatusBinding
import com.example.freshmarket.service.OrderTrackingService
import com.example.freshmarket.viewmodel.OrderHistoryViewModel
import com.example.freshmarket.viewmodel.OrderStatusViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import kotlin.math.*

class OrderStatusFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val REQUEST_CODE_LOCATION_PERMISSION = 1001
        private const val TAG = "OrderStatusFrag"
    }

    private var _binding: FragmentOrderStatusBinding? = null
    private val binding get() = _binding!!

    private val directionsRepo = com.example.freshmarket.repository.DirectionsRepository()
    private val statusViewModel: OrderStatusViewModel by activityViewModels()
    private val orderHistoryViewModel: OrderHistoryViewModel by activityViewModels()

    private var googleMap: GoogleMap? = null
    private var marketMarker: Marker? = null
    private var clientMarker: Marker? = null
    private var courierMarker: Marker? = null

    private val marketLatLng = LatLng(42.888454827221814, 74.54551664075626)
    private var clientLatLng: LatLng? = null

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private var currentOrderId: String? = null

    // BroadcastReceiver для получения обновлений из сервиса
    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == OrderTrackingService.ACTION_PROGRESS_UPDATE) {
                val orderIdFromService = intent.getStringExtra(OrderTrackingService.EXTRA_ORDER_ID)
                if (orderIdFromService == currentOrderId) {
                    val timeLeft = intent.getIntExtra(OrderTrackingService.EXTRA_TIME_LEFT, 0)
                    val lat = intent.getDoubleExtra(OrderTrackingService.EXTRA_LAT, 0.0)
                    val lng = intent.getDoubleExtra(OrderTrackingService.EXTRA_LNG, 0.0)
                    binding.tvETA.text = if (timeLeft > 0) "Осталось: $timeLeft сек" else "Заказ доставлен!"
                    val newPosition = LatLng(lat, lng)
                    if (courierMarker != null) {
                        courierMarker?.position = newPosition
                    } else {
                        courierMarker = googleMap?.addMarker(
                            MarkerOptions().position(newPosition).title("Курьер")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentOrderId = arguments?.getString("orderId")
        Log.d(TAG, "Получен orderId: $currentOrderId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION_PERMISSION
            )
        } else {
            if (!statusViewModel.isInitialized) {
                getLastKnownLocation()
            } else {
                statusViewModel.routePoints.value?.let { points ->
                    if (points.isNotEmpty()) drawRoute(points)
                }
                updateUIFromViewModel()
            }
        }

        setupButtonListeners()
        setupObservers()

        // Запускаем службу отслеживания, если заказ активен и данные маршрута получены
        if (currentOrderId != null && statusViewModel.routePoints.value?.isNotEmpty() == true) {
            // Для примера используем оставшееся время из ViewModel или задаем значение
            val totalTimeSec = statusViewModel.timeLeft.value?.let { it + 5 } ?: 60
            startOrderTrackingService(currentOrderId!!, totalTimeSec, statusViewModel.routePoints.value!!)
        }
    }

    private fun startOrderTrackingService(orderId: String, totalTimeSec: Int, points: List<LatLng>) {
        val serviceIntent = Intent(requireContext(), OrderTrackingService::class.java).apply {
            putExtra(OrderTrackingService.EXTRA_ORDER_ID, orderId)
            putExtra(OrderTrackingService.EXTRA_TOTAL_TIME, totalTimeSec)
            putParcelableArrayListExtra(OrderTrackingService.EXTRA_ROUTE_POINTS, ArrayList(points))
        }
        requireContext().startForegroundService(serviceIntent)
    }

    private fun setupButtonListeners() {
        binding.btnCallCourier.setOnClickListener {
            val phoneNumber = "+996700000000"
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }
        binding.btnCancelOrder.setOnClickListener { cancelOrder() }
    }

    private fun setupObservers() {
        statusViewModel.isAnimationRunning.observe(viewLifecycleOwner) { running ->
            if (!running && (statusViewModel.timeLeft.value ?: 0) == 0) {
                binding.tvOrderStatus.text = "Статус заказа: Заказ доставлен"
                currentOrderId?.let { id ->
                    orderHistoryViewModel.updateOrderStatus(id, "Доставлен")
                }
            } else if (running) {
                currentOrderId?.let { id ->
                    val order = orderHistoryViewModel.orders.value?.find { it.id == id }
                    if (order != null && order.status != "Курьер в пути") {
                        binding.tvOrderStatus.text = "Статус заказа: Курьер в пути"
                        orderHistoryViewModel.updateOrderStatus(id, "Курьер в пути")
                    }
                }
            }
        }

        statusViewModel.timeLeft.observe(viewLifecycleOwner) { seconds ->
            binding.tvETA.text = if (seconds > 0) "Осталось: ${formatTime(seconds)}" else "Заказ доставлен!"
        }

        statusViewModel.courierPosition.observe(viewLifecycleOwner) { pos ->
            pos?.let {
                if (courierMarker != null) {
                    courierMarker?.position = pos
                } else {
                    courierMarker = googleMap?.addMarker(
                        MarkerOptions().position(pos).title("Курьер")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                }
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(pos))
            }
        }
    }

    private fun updateUIFromViewModel() {
        statusViewModel.isAnimationRunning.value?.let { running ->
            if (running) {
                binding.tvOrderStatus.text = "Статус заказа: Курьер в пути"
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                clientLatLng = LatLng(location.latitude, location.longitude)
                fetchRouteAndDraw()
            } else {
                Toast.makeText(requireContext(), "Нет данных о местоположении", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation()
        } else {
            Toast.makeText(requireContext(), "Нужно разрешение на геолокацию", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchRouteAndDraw() {
        if (statusViewModel.isInitialized) {
            statusViewModel.routePoints.value?.let { points ->
                if (points.isNotEmpty()) drawRoute(points)
            }
            return
        }
        val clientPos = clientLatLng ?: return
        CoroutineScope(Dispatchers.Main).launch {
            val apiKey = "AIzaSyBj8bJjN7v_OA_bPDM7IweGdPYwjoBviOo"
            val points = directionsRepo.getRoutePoints(apiKey, marketLatLng, clientPos)
            if (googleMap != null && points.isNotEmpty()) {
                drawRoute(points)
                val totalSeconds = calcTotalRouteTimeSeconds(points)
                statusViewModel.setRoutePoints(points, totalSeconds, currentOrderId ?: "")
                statusViewModel.startAnimation()
            } else {
                Log.e(TAG, "Маршрут пустой или googleMap == null")
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        if (statusViewModel.isInitialized) {
            statusViewModel.routePoints.value?.let { points ->
                if (points.isNotEmpty()) drawRoute(points)
            }
        } else {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(marketLatLng, 14f))
        }
    }

    private fun drawRoute(points: List<LatLng>) {
        googleMap?.clear()
        googleMap?.addPolyline(
            PolylineOptions().addAll(points).width(5f).color(android.graphics.Color.BLUE)
        )
        marketMarker = googleMap?.addMarker(
            MarkerOptions().position(marketLatLng).title("Рынок")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        clientMarker = googleMap?.addMarker(
            MarkerOptions().position(points.last()).title("Клиент")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 14f))
    }

    private fun calcTotalRouteTimeSeconds(points: List<LatLng>): Int {
        var totalDistance = 0.0
        for (i in 0 until points.lastIndex) {
            totalDistance += haversineDistance(points[i], points[i + 1])
        }
        return (totalDistance / 2.0).toInt() // скорость 2 м/с
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

    private fun cancelOrder() {
        statusViewModel.stopAnimation()
        statusViewModel.resetState()
        googleMap?.clear()
        binding.tvOrderStatus.text = "Заказ отменён"
        binding.tvETA.text = ""
        currentOrderId?.let { id ->
            Log.d(TAG, "Обновление заказа $id на статус 'Отменён'")
            orderHistoryViewModel.updateOrderStatus(id, "Отменён")
        }
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return if (m > 0) "$m мин $s сек" else "$s сек"
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        // Указываем флаг RECEIVER_NOT_EXPORTED для динамической регистрации
        val filter = IntentFilter(OrderTrackingService.ACTION_PROGRESS_UPDATE)
        requireContext().registerReceiver(progressReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        requireContext().unregisterReceiver(progressReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
