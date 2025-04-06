package com.example.freshmarket.view.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.freshmarket.R
import com.example.freshmarket.viewmodel.OrderHistoryViewModel
import com.example.freshmarket.worker.OrderStatusWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val ACTION_OPEN_ORDER = "com.example.freshmarket.OPEN_ORDER"
        const val EXTRA_ORDER_ID = "orderId"
        const val NOTIFICATION_CHANNEL_ID = "order_channel_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate() called")
        createNotificationChannel()
        setupWorkManager()
        setupNavigation()
        Log.d(TAG, "MainActivity onCreate called, intent=$intent")
        intent?.let {
            Log.d(TAG, "onCreate received intent: ${it.data}")
            handleDeepLinkIntent(it)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent() called with action: ${intent.action}")
        Log.d(TAG, "MainActivity onNewIntent called, intent=$intent")

        if (intent.action == "com.example.freshmarket.TRACK_ORDER") {
            // Получаем OrderHistoryViewModel через ViewModelProvider
            val orderHistoryViewModel = ViewModelProvider(this)[OrderHistoryViewModel::class.java]
            // Пытаемся найти активный заказ (например, статус содержит "Оформлен" или "Курьер")
            val activeOrder = orderHistoryViewModel.orders.value?.find { order ->
                order.status.contains("Оформлен") || order.status.contains("Курьер")
            }
            if (activeOrder != null) {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val bundle = bundleOf("orderId" to activeOrder.id)
                navHostFragment.navController.navigate(R.id.orderStatusFragment, bundle)
            } else {
                Toast.makeText(this, "Нет актуальных заказов", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Если другое действие – обрабатываем стандартную deep link логику
            handleDeepLinkIntent(intent)
        }
    }


    private fun handleDeepLinkIntent(intent: Intent) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val currentDestination = navHostFragment.navController.currentDestination?.id

        if (currentDestination == R.id.orderStatusFragment) {
            // Already on order status fragment, no need to navigate again
            return
        }

        navHostFragment.navController.handleDeepLink(intent)
    }

    private fun setupNavigation() {
        Log.d(TAG, "Setting up navigation")
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val onboardingShown = prefs.getBoolean("onboarding_shown", false)
        val currentUser = FirebaseAuth.getInstance().currentUser

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(
            when {
                !onboardingShown -> R.id.onboardingFragment
                currentUser == null -> R.id.loginFragment
                else -> R.id.nav_home
            }
        )
        navController.graph = navGraph

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNav, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = when (destination.id) {
                R.id.onboardingFragment, R.id.loginFragment, R.id.registerFragment -> View.GONE
                else -> View.VISIBLE
            }
            Log.d(TAG, "Navigated to ${destination.label}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Заказы",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о заказах"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun setupWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<OrderStatusWorker>(
            2, TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(this).enqueue(workRequest)
        Log.d(TAG, "WorkManager setup complete")
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity onPause() called")
    }
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity onStop() called")
    }
}