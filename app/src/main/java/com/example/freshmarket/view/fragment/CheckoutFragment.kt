    package com.example.freshmarket.view.fragment

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.activityViewModels
    import androidx.navigation.fragment.findNavController
    import com.example.freshmarket.R
    import com.example.freshmarket.databinding.FragmentCheckoutBinding
    import com.example.freshmarket.viewmodel.CartViewModel
    // Обратите внимание: OrderHistoryViewModel здесь не используется для создания заказа
    class CheckoutFragment : Fragment() {

        private var _binding: FragmentCheckoutBinding? = null
        private val binding get() = _binding!!

        private val cartViewModel: CartViewModel by activityViewModels()
        // Мы не добавляем заказ здесь, чтобы не создавать дубликаты

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.progressBar.visibility = View.GONE

            binding.btnConfirmOrder.setOnClickListener {
                val name = binding.etName.text.toString().trim()
                val address = binding.etAddress.text.toString().trim()

                if (name.isEmpty() || address.isEmpty()) {
                    Toast.makeText(requireContext(), "Заполните все поля!", Toast.LENGTH_SHORT).show()
                } else {
                    // Здесь можно сохранить введённые данные в shared ViewModel или передать через SafeArgs
                    // и перейти к выбору оплаты.
                    findNavController().navigate(R.id.paymentChoiceFragment)
                }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
