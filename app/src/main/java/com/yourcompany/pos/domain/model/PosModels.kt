package com.yourcompany.pos.domain.model

data class PosUiProduct(
    val id: Long,
    val sku: String,
    val name: String,
    val price: Double,
    val taxRate: Double,
    val imageUrl: String? = null,
    val category: String = "未分類",
    val stockQuantity: Int = -1,
    val lowStockThreshold: Int = 10,
    val isActive: Boolean = true
)

data class PosUiCartItem(
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val sku: String,
    val unitPrice: Double,
    val taxRate: Double,
    val quantity: Int,
    val imageUrl: String? = null,
    val modifiers: List<String> = emptyList(),
    val note: String? = null,
    val customDiscount: Double = 0.0
) {
    val subtotal: Double
        get() = (unitPrice * quantity) - customDiscount
}
