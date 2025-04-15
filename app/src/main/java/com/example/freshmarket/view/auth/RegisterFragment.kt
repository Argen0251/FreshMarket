package com.example.freshmarket.view.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.freshmarket.R
import com.example.freshmarket.databinding.FragmentRegisterBinding
import com.example.freshmarket.view.main.MainActivity
import com.example.freshmarket.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Следим за результатом регистрации
        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            if (result.isSuccess) {
                (requireActivity() as? AuthActivity)?.goToMainActivity()
                    ?: run {
                        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        requireActivity().finish()
                    }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Ошибка: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("RegisterFragment", "Ошибка регистрации", result.exceptionOrNull())
            }
        }

        binding.btnRegisterUser.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Регистрируем пользователя
            authViewModel.register(email, password)
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
