package com.example.freshmarket.ui.auth


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.freshmarket.R
import com.example.freshmarket.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth) // В этом layout должен быть контейнер с id "auth_container"

        firebaseAuth = FirebaseAuth.getInstance()

        // Если пользователь уже авторизован, переходим в MainActivity
        if (firebaseAuth.currentUser != null) {
            goToMainActivity()
            return
        }

        // Получаем SharedPreferences
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val onboardingShown = prefs.getBoolean("onboarding_shown", false)

        // Находим NavHostFragment и загружаем nav_graph.xml
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.auth_container) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        // Динамически задаем стартовую точку
        if (!onboardingShown) {
            navGraph.setStartDestination(R.id.onboardingFragment)
        } else {
            navGraph.setStartDestination(R.id.loginFragment)
        }
        navController.graph = navGraph
    }

    fun goToMainActivity() {
        // Переход в MainActivity (главное приложение)
        // Например, запускаем MainActivity и завершаем AuthActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
