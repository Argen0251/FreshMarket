package com.example.freshmarket.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshmarket.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _authResult = MutableLiveData<Result<FirebaseUser?>>()
    val authResult: LiveData<Result<FirebaseUser?>> = _authResult

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    // Регистрация
    fun register(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password)
            _authResult.value = result
        }
    }

    // Логин (email/password)
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            _authResult.value = result
        }
    }

    // Логин (Google)
    fun loginWithGoogle(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            val result = authRepository.firebaseAuthWithGoogle(account)
            _authResult.value = result
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
