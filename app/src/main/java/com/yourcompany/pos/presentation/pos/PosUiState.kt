package com.yourcompany.pos.presentation.pos

import com.yourcompany.pos.domain.model.CheckoutSummary
import com.yourcompany.pos.domain.model.CartItem
import com.yourcompany.pos.domain.model.Order
import com.yourcompany.pos.domain.model.Product
import com.yourcompany.pos.domain.model.PaymentMethod

data class PosUiState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val checkoutOnlineOrder: Order? = null,
    val cartItems: List<CartItem> = emptyList(),
    val recentOrders: List<Order> = emptyList(),
    val selectedProduct: Product? = null,
    val searchQuery: String = "",
    val orderType: String = "外帶",
    val tableNumber: String? = null,
    val globalDiscount: Double = 0.0,
    val globalTaxRate: Double = 0.05,
    val cashReceivedInput: String = "",
    val showCheckoutDialog: Boolean = false,
    val isCheckoutScreenActive: Boolean = false,
    val showMemberManagerDialog: Boolean = false,
    val showMemberTopUpDialog: Boolean = false,
    val memberBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val isCheckingOut: Boolean = false,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val checkoutSummary: CheckoutSummary = CheckoutSummary(
        subtotal = 0.0,
        taxAmount = 0.0,
        discountAmount = 0.0,
        totalAmount = 0.0,
        cashReceivedAmount = 0.0,
        changeAmount = 0.0,
        itemCount = 0
    ),
    val snackbarMessage: String? = null,
    val printReceipt: Boolean = true,
    val errorMessage: String? = null,
    val memberId: String? = null,
    val memberName: String? = null,
    val memberPoints: Int? = null,
    val memberDiscountRate: Double = 1.0,
    val memberSource: String? = null,
    val productTagSku: String? = null,
    val productTagName: String? = null,
    val nfcDetails: String = "",
    val nfcAvailable: Boolean = false,
    val nfcEnabled: Boolean = false,
    val nfcReading: Boolean = false,
    val printerConnected: Boolean = false,
    val printerConnecting: Boolean = false,
    val printerPrinting: Boolean = false,
    val nfcStatus: String = "",
    val printerStatus: String = ""
)
