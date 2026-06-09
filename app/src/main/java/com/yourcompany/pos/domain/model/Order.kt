package com.yourcompany.pos.domain.model

enum class OrderStatus {
    PENDING,
    PAID,
    PREPARING,
    CANCELLED,
    SYNCED,
    HELD,
    COMPLETED
}

enum class PaymentMethod {
    CASH,
    CARD,
    NFC,
    MIXED,
    MEMBER_BALANCE
}

data class OrderLine(
    val id: Long = 0,
    val orderId: Long = 0,
    val productId: Long,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: Double,
    val note: String? = null,
    val modifiers: List<String> = emptyList()
) {
    val lineSubtotal: Double
        get() = unitPrice * quantity

    val lineTax: Double
        get() = lineSubtotal * taxRate

    val lineTotal: Double
        get() = lineSubtotal + lineTax
}

data class Order(
    val id: Long = 0,
    val orderNo: String,
    val totalAmount: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val globalDiscount: Double = 0.0,
    val orderType: String = "外帶", // 內用 or 外帶
    val tableNumber: String? = null,
    val paymentMethod: PaymentMethod,
    val status: OrderStatus,
    val syncStatus: Boolean = false,
    val remoteOrderId: String? = null,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CheckoutSummary(
    val subtotal: Double,
    val taxAmount: Double,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val cashReceivedAmount: Double = 0.0,
    val changeAmount: Double = 0.0,
    val itemCount: Int
)
