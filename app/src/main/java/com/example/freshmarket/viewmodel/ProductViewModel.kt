package com.example.freshmarket.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.data.model.Category
import com.example.freshmarket.repository.FirestoreProductRepository
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val repository = FirestoreProductRepository()

    private val _productsByCategory = MutableLiveData<List<Product>>()
    val productsByCategory: LiveData<List<Product>> get() = _productsByCategory

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    fun loadProductsByCategory(categoryName: String) {
        viewModelScope.launch {
            val products = repository.getProductsByCategory(categoryName)
            _productsByCategory.postValue(products)
        }
    }

    fun loadAllProducts() {
        viewModelScope.launch {
            val products = repository.getAllProducts()
            _productsByCategory.postValue(products)
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            val cats = repository.getAllCategories()
            _categories.postValue(cats)
        }
    }
}
