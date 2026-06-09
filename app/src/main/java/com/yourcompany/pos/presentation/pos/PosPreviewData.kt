package com.yourcompany.pos.presentation.pos

import com.yourcompany.pos.domain.model.CartItem
import com.yourcompany.pos.domain.model.CheckoutSummary
import com.yourcompany.pos.domain.model.Order
import com.yourcompany.pos.domain.model.Product

object PosPreviewData {
    fun sampleState(): PosUiState {
        val products = listOf(
            Product(1, "DRK-001", "經典奶茶", 65.0, 0.05),
            Product(2, "DRK-002", "美式咖啡", 55.0, 0.05),
            Product(3, "FOD-101", "雞肉三明治", 95.0, 0.05),
            Product(4, "FOD-104", "鮪魚潛艇堡", 118.0, 0.05),
            Product(5, "SNK-203", "能量飲料", 49.0, 0.05),
            Product(6, "ADD-302", "加鮮奶", 10.0, 0.05)
        )

        val cartItems = listOf(
            CartItem(id = 1, productId = 1, productName = "經典奶茶", sku = "DRK-001", unitPrice = 65.0, taxRate = 0.05, quantity = 2),
            CartItem(id = 2, productId = 3, productName = "雞肉三明治", sku = "FOD-101", unitPrice = 95.0, taxRate = 0.05, quantity = 1),
            CartItem(id = 3, productId = 5, productName = "能量飲料", sku = "SNK-203", unitPrice = 49.0, taxRate = 0.05, quantity = 1)
        )

        val subtotal = cartItems.sumOf { it.subtotal }
        val tax = cartItems.sumOf { it.taxAmount }
        val discount = subtotal * 0.05
        val total = subtotal + tax - discount
        val cash = 1000.0
        val change = cash - total

        return PosUiState(
            products = products,
            cartItems = cartItems,
            recentOrders = listOf(
                Order(
                    orderNo = "POS-20260606-0001",
                    totalAmount = total,
                    taxAmount = tax,
                    discountAmount = discount,
                    paymentMethod = com.yourcompany.pos.domain.model.PaymentMethod.CASH,
                    status = com.yourcompany.pos.domain.model.OrderStatus.PAID
                )
            ),
            searchQuery = "",
            cashReceivedInput = cash.toString(),
            isLoading = false,
            isCheckingOut = false,
            checkoutSummary = CheckoutSummary(
                subtotal = subtotal,
                taxAmount = tax,
                discountAmount = discount,
                totalAmount = total,
                cashReceivedAmount = cash,
                changeAmount = change,
                itemCount = cartItems.sumOf { it.quantity }
            ),
            snackbarMessage = "",
            errorMessage = null,
            memberId = "M-10086",
            memberName = "王小明",
            memberPoints = 1280,
            memberDiscountRate = 0.95,
            memberSource = "{" + "\"type\":\"member\",\"memberId\":\"M-10086\",\"name\":\"王小明\",\"points\":1280,\"discountRate\":0.95}" ,
            productTagSku = "DRK-001",
            productTagName = "經典奶茶",
            nfcDetails = "Points 1280 | Discount 5%",
            nfcAvailable = true,
            nfcEnabled = true,
            nfcReading = true,
            printerConnected = true,
            printerConnecting = false,
            printerPrinting = false,
            nfcStatus = "會員卡：王小明 (M-10086)",
            printerStatus = "印表機已連線"
        )
    }
}
