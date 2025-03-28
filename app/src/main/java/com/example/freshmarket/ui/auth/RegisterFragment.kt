package com.example.freshmarket.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.freshmarket.R
import com.example.freshmarket.databinding.FragmentRegisterBinding
import com.example.freshmarket.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth

    // Используем backing property для binding
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализируем FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Инициализация логики в onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegisterUser.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etRegisterEmail.text.toString()
            val password = binding.etRegisterPassword.text.toString()

            // Простейшая проверка полей
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                // TODO: вывести сообщение об ошибке
                return@setOnClickListener
            }

            // Регистрируем пользователя через Firebase
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Вход успешно!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.nav_home)
                    } else {
                        Toast.makeText(requireContext(), "Ошибка: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Login", "Ошибка входа: ${e.localizedMessage}")
                }
        }

        // Кнопка «Назад» — возвращаемся к LoginFragment
        binding.btnBackToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
