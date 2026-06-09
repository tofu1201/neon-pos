package com.yourcompany.pos.nfc

sealed interface NfcTagContent {
    data class MemberCard(
        val memberId: String,
        val memberName: String,
        val points: Int = 0,
        val discountRate: Double = 1.0,
        val rawPayload: String? = null
    ) : NfcTagContent

    data class ProductTag(
        val sku: String,
        val productName: String,
        val price: Double? = null,
        val category: String? = null,
        val rawPayload: String? = null
    ) : NfcTagContent

    data class PlainText(val value: String) : NfcTagContent
    data class UriContent(val value: String) : NfcTagContent
    data class Unknown(val rawPayload: String? = null) : NfcTagContent
}
