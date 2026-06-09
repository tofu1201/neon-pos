package com.yourcompany.pos.domain.model

data class Product(
    val id: Long,
    val sku: String,
    val name: String,
    val price: Double,
    val taxRate: Double,
    val imageUrl: String? = null,
    val category: String = "未分類",
    val stockQuantity: Int = -1,
    val lowStockThreshold: Int = 10,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
