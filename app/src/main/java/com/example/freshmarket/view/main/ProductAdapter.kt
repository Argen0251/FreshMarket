package com.example.freshmarket.view.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freshmarket.R
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.databinding.ItemProductBinding

class ProductAdapter(
    private var products: List<Product>,
    private val onProductClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            product: Product,
            onProductClick: (Product) -> Unit,
            onAddToCartClick: (Product) -> Unit
        ) {
            binding.tvName.text = product.name
            binding.tvPrice.text = "Цена: ${product.priceCents}"

            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_load)
                .error(R.drawable.ic_fr_mar)
                .into(binding.ivProductImage)

            // Нажатие на весь элемент → открыть детали
            binding.root.setOnClickListener {
                onProductClick(product)
            }

            // Кнопка «В корзину»
            binding.btnAddToCart.setOnClickListener {
                onAddToCartClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position], onProductClick, onAddToCartClick)
    }

    override fun getItemCount() = products.size

    fun updateList(newProducts: List<Product>) {
        this.products = newProducts
        notifyDataSetChanged()
    }
}
