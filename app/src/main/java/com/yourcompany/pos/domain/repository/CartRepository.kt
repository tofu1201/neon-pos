package com.yourcompany.pos.domain.repository

import com.yourcompany.pos.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun observeCartItems(): Flow<List<CartItem>>
    suspend fun addToCart(item: CartItem)
    suspend fun updateQuantity(productId: Long, quantity: Int)
    suspend fun removeItem(productId: Long)
    suspend fun clearCart()
}
