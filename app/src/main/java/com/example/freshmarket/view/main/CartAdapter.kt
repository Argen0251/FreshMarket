package com.example.freshmarket.view.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.freshmarket.R
import com.example.freshmarket.data.model.CartItem
import com.example.freshmarket.databinding.ItemCartProductBinding

/**
 * Адаптер для отображения списка CartItem
 */
class CartAdapter(
    private var items: List<CartItem>,
    private val onQuantityChange: (productId: String, newQuantity: Double) -> Unit,
    private val onRemove: (productId: String) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(private val binding: ItemCartProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem,
                 onQuantityChange: (String, Double) -> Unit,
                 onRemove: (String) -> Unit
        ) {
            val product = cartItem.product

            binding.tvCartProductName.text = product.name

            // Покажем "кол-во: X" и "Общая стоимость"
            val totalPriceItem = cartItem.product.priceCents * cartItem.quantity
            binding.tvCartProductPrice.text = "Итого: $totalPriceItem"

            binding.tvQuantity.text = cartItem.quantity.toString()

            // Загрузка картинки
            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_load)
                .error(R.drawable.ic_fr_mar)
                .into(binding.ivCartProductImage)

            // Кнопки +/-
            binding.btnIncrease.setOnClickListener {
                val newQuantity = cartItem.quantity + 1.0 // или + 0.5, если надо на вес
                onQuantityChange(product.id, newQuantity)
            }
            binding.btnDecrease.setOnClickListener {
                val newQuantity = cartItem.quantity - 1.0
                onQuantityChange(product.id, newQuantity.coerceAtLeast(0.0))
            }

            // Удалить товар
            binding.btnRemove.setOnClickListener {
                onRemove(product.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position], onQuantityChange, onRemove)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
