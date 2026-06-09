package com.yourcompany.pos.presentation.pos

import com.yourcompany.pos.domain.model.PaymentMethod

sealed interface PosEvent {
    data class SelectProduct(val productId: Long) : PosEvent
    data class AddToCart(val productId: Long, val modifiers: List<String> = emptyList(), val note: String? = null) : PosEvent
    data class UpdateQuantity(val cartItemId: Long, val quantity: Int) : PosEvent
    data class RemoveItem(val cartItemId: Long) : PosEvent
    data class SearchChanged(val query: String) : PosEvent
    data object ClearCart : PosEvent
    data class Checkout(val paymentMethod: PaymentMethod, val printReceipt: Boolean) : PosEvent
    data class TogglePrintReceipt(val print: Boolean) : PosEvent
    data object StartNfcScan : PosEvent
    data object StopNfcScan : PosEvent
    data object ConnectPrinter : PosEvent
    data object PrintDemoReceipt : PosEvent
    data class CashReceivedChanged(val value: String) : PosEvent
    data object DismissMessage : PosEvent
    data object DismissAlert : PosEvent
    data class NfcStatusChanged(val message: String) : PosEvent
    data class PrinterStatusChanged(val message: String) : PosEvent
    data class AddProduct(val sku: String, val name: String, val price: Double, val taxRate: Double) : PosEvent
    data class ShowCheckoutDialog(val show: Boolean) : PosEvent
    data class ShowMemberManagerDialog(val show: Boolean) : PosEvent
    data class ShowMemberTopUpDialog(val show: Boolean) : PosEvent
    data class SearchMember(val phone: String) : PosEvent
    data class RegisterMember(val phone: String, val name: String) : PosEvent
    data class TopUpMember(val amount: Double) : PosEvent
    data class BindNfcCard(val phone: String) : PosEvent
    data class SelectPaymentMethod(val method: com.yourcompany.pos.domain.model.PaymentMethod) : PosEvent
    data class ScanOnlineOrder(val orderNo: String) : PosEvent
    data class SelectCategory(val category: String?) : PosEvent
    data class SetOrderType(val type: String, val tableNumber: String? = null) : PosEvent
    data class SetGlobalDiscount(val discount: Double) : PosEvent
    object HoldOrder : PosEvent
    object RetrieveOrder : PosEvent
    object PrintZReport : PosEvent
    object NavigateToCheckout : PosEvent
    object NavigateToSettings : PosEvent
    data class SetCheckoutScreenActive(val active: Boolean) : PosEvent
    data class CancelOrder(val orderId: Long) : PosEvent
    object ClearCompletedOrderNo : PosEvent
    data class LoginEmployee(val pin: String) : PosEvent
    object LogoutEmployee : PosEvent
    data class CancelOrderAttempt(val orderId: Long) : PosEvent
    data class VerifyAdminPinForCancel(val pin: String) : PosEvent
    object DismissAdminPinDialog : PosEvent
}
