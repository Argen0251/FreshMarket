    package com.example.freshmarket.view.fragment

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.core.os.bundleOf
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.activityViewModels
    import androidx.navigation.fragment.findNavController
    import com.example.freshmarket.R
    import com.example.freshmarket.databinding.FragmentProfileBinding
    import com.example.freshmarket.viewmodel.OrderHistoryViewModel
    import com.google.firebase.auth.FirebaseAuth

    class ProfileFragment : Fragment() {

        private var _binding: FragmentProfileBinding? = null
        private val binding get() = _binding!!
        private val firebaseAuth = FirebaseAuth.getInstance()

        // Получаем общий экземпляр OrderHistoryViewModel
        private val orderHistoryViewModel: OrderHistoryViewModel by activityViewModels()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentProfileBinding.inflate(inflater, container, false)
            return binding.root
        }

        // Инициализация профиля и установка слушателей кнопок
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val currentUser = firebaseAuth.currentUser
            currentUser?.let {
                binding.tvUserName.text = it.displayName ?: "Неизвестно"
                binding.tvUserEmail.text = it.email ?: "No email"
            }

            // Переход к истории заказов
            binding.btnOrderHistory.setOnClickListener {
                findNavController().navigate(R.id.orderHistoryFragment)
            }

            // Новая кнопка для отслеживания активного заказа
            binding.btnTrackOrder.setOnClickListener {
                // Пытаемся найти активный заказ (например, статус содержит "Оформлен" или "Курьер")
                val activeOrder = orderHistoryViewModel.orders.value?.find { order ->
                    order.status.contains("Оформлен") || order.status.contains("Курьер")
                }
                if (activeOrder != null) {
                    // Если активный заказ найден, переходим на экран статуса заказа,
                    // передавая orderId через Bundle
                    val bundle = bundleOf("orderId" to activeOrder.id)
                    findNavController().navigate(R.id.orderStatusFragment, bundle)
                } else {
                    Toast.makeText(requireContext(), "Нет актуальных заказов", Toast.LENGTH_SHORT).show()
                }
            }

            // Переход на экран выхода из профиля (логина)
            binding.btnLogout.setOnClickListener {
                firebaseAuth.signOut()
                findNavController().navigate(R.id.loginFragment)
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
