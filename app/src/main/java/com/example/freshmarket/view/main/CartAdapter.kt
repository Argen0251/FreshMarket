    package com.example.freshmarket.view.main

    import android.view.LayoutInflater
    import android.view.ViewGroup
    import androidx.recyclerview.widget.RecyclerView
    import com.bumptech.glide.Glide
    import com.example.freshmarket.R
    import com.example.freshmarket.data.model.Product
    import com.example.freshmarket.databinding.ItemCartProductBinding

    class CartAdapter(
        private var items: List<Product>,
        private val onRemoveClick: (Product) -> Unit
    ) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

        class CartViewHolder(private val binding: ItemCartProductBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(product: Product, onRemoveClick: (Product) -> Unit) {
                binding.tvCartProductName.text = product.name
                binding.tvCartProductPrice.text = "Цена: ${product.priceCents}"

                Glide.with(binding.root.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_load)
                    .error(R.drawable.ic_fr_mar)
                    .into(binding.ivCartProductImage)

                binding.btnRemove.setOnClickListener {
                    onRemoveClick(product)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
            val binding = ItemCartProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return CartViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
            holder.bind(items[position], onRemoveClick)
        }

        override fun getItemCount() = items.size

        fun updateItems(newItems: List<Product>) {
            items = newItems
            notifyDataSetChanged()
        }
    }
