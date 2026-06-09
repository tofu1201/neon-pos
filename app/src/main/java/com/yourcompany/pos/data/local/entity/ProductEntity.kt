package com.yourcompany.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourcompany.pos.domain.model.Product
import com.yourcompany.pos.domain.model.PosUiProduct

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String,
    val name: String,
    val price: Double,
    val taxRate: Double,
    val imageUrl: String? = null,
    val category: String = "未分類",
    val stockQuantity: Int = -1, // -1 means unlimited
    val lowStockThreshold: Int = 10,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Product = Product(
        id = id,
        sku = sku,
        name = name,
        price = price,
        taxRate = taxRate,
        imageUrl = imageUrl,
        category = category,
        stockQuantity = stockQuantity,
        lowStockThreshold = lowStockThreshold,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun toUiModel(): PosUiProduct = PosUiProduct(
        id = id,
        sku = sku,
        name = name,
        price = price,
        taxRate = taxRate,
        imageUrl = imageUrl,
        category = category,
        stockQuantity = stockQuantity,
        lowStockThreshold = lowStockThreshold,
        isActive = isActive
    )

    companion object {
        fun fromDomain(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            sku = product.sku,
            name = product.name,
            price = product.price,
            taxRate = product.taxRate,
            imageUrl = product.imageUrl,
            category = product.category,
            stockQuantity = product.stockQuantity,
            lowStockThreshold = product.lowStockThreshold,
            isActive = product.isActive,
            createdAt = product.createdAt,
            updatedAt = product.updatedAt
        )
    }
}
