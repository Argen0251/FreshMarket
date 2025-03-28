package com.example.freshmarket.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.freshmarket.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Проверка, показан ли онбординг
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val onboardingShown = prefs.getBoolean("onboarding_shown", false)

        // Проверка авторизации
        val currentUser = FirebaseAuth.getInstance().currentUser

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        // Определяем, какой фрагмент стартовый
        if (!onboardingShown) {
            // Первый запуск -> Onboarding
            navGraph.setStartDestination(R.id.onboardingFragment)
        } else {
            // Онбординг уже показан
            if (currentUser == null) {
                navGraph.setStartDestination(R.id.loginFragment)
            } else {
                navGraph.setStartDestination(R.id.nav_home)
            }
        }
        navController.graph = navGraph

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        // Привязка BottomNavigation
        NavigationUI.setupWithNavController(bottomNav, navController)

        // Скрываем BottomNav на онбординге/логине/регистрации
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.onboardingFragment,
                R.id.loginFragment,
                R.id.registerFragment -> {
                    bottomNav.visibility = View.GONE
                }
                else -> {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}
