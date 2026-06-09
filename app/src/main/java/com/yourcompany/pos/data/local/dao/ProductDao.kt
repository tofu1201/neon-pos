package com.yourcompany.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.yourcompany.pos.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun observeActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE sku LIKE '%' || :keyword || '%' OR name LIKE '%' || :keyword || '%' ORDER BY updatedAt DESC")
    fun searchProducts(keyword: String): Flow<List<ProductEntity>>

    @Upsert
    suspend fun upsert(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :amount WHERE id = :productId AND stockQuantity != -1")
    suspend fun updateStock(productId: Long, amount: Int)

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)

    @Query("DELETE FROM products")
    suspend fun clearAll()
}
