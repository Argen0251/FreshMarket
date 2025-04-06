    package com.example.freshmarket.view.fragment

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.activityViewModels
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.GridLayoutManager
    import com.example.freshmarket.R
    import com.example.freshmarket.databinding.FragmentFavoriteBinding
    import com.example.freshmarket.view.main.FavoriteAdapter
    import com.example.freshmarket.viewmodel.FavoriteViewModel

    class FavoriteFragment : Fragment() {

        private var _binding: FragmentFavoriteBinding? = null
        private val binding get() = _binding!!

        // Используем activityViewModels, чтобы избранное было доступно глобально
        private val favoriteViewModel: FavoriteViewModel by activityViewModels()

        private lateinit var favoriteAdapter: FavoriteAdapter

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
            return binding.root
        }

        // Инициализация RecyclerView и подписка на обновления избранного
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Настраиваем RecyclerView с GridLayoutManager (2 колонки)
            binding.rvFavoriteItems.layoutManager = GridLayoutManager(requireContext(), 2)

            favoriteAdapter = FavoriteAdapter(
                items = emptyList(),
                // При клике на кнопку удаления удаляем товар из избранного
                onRemoveClick = { product ->
                    favoriteViewModel.removeFromFavorites(product)
                    Toast.makeText(requireContext(), "Удалено из избранного: ${product.name}", Toast.LENGTH_SHORT).show()
                },
                // При клике на товар переходим к экрану деталей
                onProductClick = { product ->
                    val action = FavoriteFragmentDirections.actionNavFavoriteToProductDetailsFragment(
                        productId = product.id,
                        productName = product.name,
                        productDescription = product.description,
                        productPrice = product.priceCents,
                        productImageUrl = product.imageUrl
                    )
                    findNavController().navigate(action)
                }
            )

            binding.rvFavoriteItems.adapter = favoriteAdapter

            // Подписываемся на LiveData с избранными товарами
            favoriteViewModel.favoriteItems.observe(viewLifecycleOwner) { items ->
                favoriteAdapter.updateItems(items)
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
