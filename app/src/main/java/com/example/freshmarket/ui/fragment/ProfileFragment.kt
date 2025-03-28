package com.example.freshmarket.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.freshmarket.R
import com.example.freshmarket.databinding.FragmentProfileBinding
import com.example.freshmarket.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    // Используем backing property для binding
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Выполняем инициализацию в onViewCreated, когда View уже создано
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Заполняем поля из FirebaseAuth.currentUser
        val currentUser = firebaseAuth.currentUser
        currentUser?.let {
            binding.tvUserName.text = it.displayName ?: "Неизвестно"
            binding.tvUserEmail.text = it.email ?: "No email"
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // Возвращаемся на экран логина
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
