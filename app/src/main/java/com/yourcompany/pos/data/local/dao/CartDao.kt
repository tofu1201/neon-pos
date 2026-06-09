package com.yourcompany.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.yourcompany.pos.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY updatedAt DESC")
    fun observeCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE id = :id LIMIT 1")
    suspend fun getCartItemById(id: Long): CartItemEntity?

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getCartItemsByProductId(productId: Long): List<CartItemEntity>

    @Upsert
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteByProductId(productId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
