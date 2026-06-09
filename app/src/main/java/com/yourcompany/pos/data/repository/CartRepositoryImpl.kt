package com.yourcompany.pos.data.repository

import com.yourcompany.pos.data.local.dao.CartDao
import com.yourcompany.pos.data.local.entity.CartItemEntity
import com.yourcompany.pos.domain.model.CartItem
import com.yourcompany.pos.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepositoryImpl(
    private val cartDao: CartDao
) : CartRepository {
    override fun observeCartItems(): Flow<List<CartItem>> =
        cartDao.observeCartItems().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addToCart(item: CartItem) {
        val existingItems = cartDao.getCartItemsByProductId(item.productId)
        val existing = existingItems.find { 
            it.note == item.note && 
            it.modifiersRaw == item.modifiers.joinToString("||")
        }
        
        val merged = if (existing != null) {
            existing.copy(
                productName = item.productName,
                sku = item.sku,
                unitPrice = item.unitPrice,
                taxRate = item.taxRate,
                quantity = existing.quantity + item.quantity,
                imageUrl = item.imageUrl,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            CartItemEntity.fromDomain(item)
        }
        cartDao.upsert(merged)
    }

    override suspend fun updateQuantity(cartItemId: Long, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteById(cartItemId)
            return
        }

        val existing = cartDao.getCartItemById(cartItemId) ?: return
        cartDao.upsert(
            existing.copy(
                quantity = quantity,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeItem(cartItemId: Long) {
        cartDao.deleteById(cartItemId)
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
