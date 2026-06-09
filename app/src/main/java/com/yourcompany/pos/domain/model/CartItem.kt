package com.yourcompany.pos.domain.model

data class CartItem(
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

    val taxAmount: Double
        get() = subtotal * taxRate

    val total: Double
        get() = subtotal + taxAmount
}
