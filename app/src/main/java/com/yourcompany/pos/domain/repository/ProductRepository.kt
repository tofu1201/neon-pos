package com.yourcompany.pos.domain.repository

import com.yourcompany.pos.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>
    fun searchProducts(keyword: String): Flow<List<Product>>
    suspend fun getProductById(productId: Long): Product?
    suspend fun upsertProduct(product: Product)
    suspend fun upsertProducts(products: List<Product>)
    suspend fun deleteProductById(productId: Long)
    suspend fun clearAll()
    suspend fun updateStock(productId: Long, amount: Int)
}
