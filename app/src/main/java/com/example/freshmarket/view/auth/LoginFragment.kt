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
import com.example.freshmarket.databinding.FragmentLoginBinding
import com.example.freshmarket.view.main.MainActivity
import com.example.freshmarket.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private val RC_SIGN_IN = 100

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Наблюдаем за результатом аутентификации
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
                val message = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                Toast.makeText(requireContext(), "Ошибка входа: $message", Toast.LENGTH_SHORT).show()
                Log.e("LoginFragment", "Ошибка входа", result.exceptionOrNull())
            }
        }

        // Кнопка «Войти»
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Введите email и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.login(email, password)
        }

        // Переход к регистрации
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // Google Sign‑In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        binding.btnGoogleSignIn.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            authViewModel.loginWithGoogle(account)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
