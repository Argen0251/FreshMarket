package com.example.freshmarket.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freshmarket.data.model.Category
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.databinding.FragmentProductListBinding
import com.example.freshmarket.view.main.CategoriesAdapter
import com.example.freshmarket.view.main.ProductAdapter
import com.example.freshmarket.viewmodel.CartViewModel
import com.example.freshmarket.viewmodel.ProductViewModel
import androidx.fragment.app.activityViewModels
import com.example.freshmarket.view.fragment.ProductListFragmentDirections

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    // ViewModel для списка товаров
    private val productViewModel: ProductViewModel by viewModels()

    // ViewModel для корзины (чтобы при клике «Добавить в корзину» вызывать addToCart)
    private val cartViewModel: CartViewModel by activityViewModels()

    private lateinit var productAdapter: ProductAdapter
    private var categoriesAdapter: CategoriesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Инициализация
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка RecyclerView для товаров (сетка из 2-х колонок)
        productAdapter = ProductAdapter(
            products = emptyList(),
            onProductClick = { product ->
                // Переход к деталям
                val action = ProductListFragmentDirections.actionNavHomeToProductDetailsFragment(
                    productId = product.id,
                    productName = product.name,
                    productDescription = product.description,
                    productPrice = product.priceCents,
                    productImageUrl = product.imageUrl
                )
                findNavController().navigate(action)
            },
            onAddToCartClick = { product ->
                // Добавление в корзину
                cartViewModel.addToCart(product)
                Toast.makeText(requireContext(), "Товар добавлен в корзину!", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = productAdapter

        // Подписываемся на список товаров
        productViewModel.productsByCategory.observe(viewLifecycleOwner) { products ->
            productAdapter.updateList(products)
        }

        // Подписываемся на список категорий
        productViewModel.categories.observe(viewLifecycleOwner) { cats ->
            val allCategory = Category("Все", "https://example.com/images/all.png")
            val fullCategoryList = listOf(allCategory) + cats

            categoriesAdapter = CategoriesAdapter(fullCategoryList) { selectedCategory ->
                if (selectedCategory.name == "Все") {
                    productViewModel.loadAllProducts()
                } else {
                    productViewModel.loadProductsByCategory(selectedCategory.name)
                }
            }
            binding.categoriesRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.categoriesRecyclerView.adapter = categoriesAdapter
        }

        // Загружаем категории и все товары
        productViewModel.loadCategories()
        productViewModel.loadAllProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
