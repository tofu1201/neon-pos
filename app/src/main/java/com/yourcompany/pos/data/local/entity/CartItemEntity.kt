package com.yourcompany.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourcompany.pos.domain.model.CartItem
import com.yourcompany.pos.domain.model.PosUiCartItem

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val sku: String,
    val unitPrice: Double,
    val taxRate: Double,
    val quantity: Int,
    val imageUrl: String? = null,
    val modifiersRaw: String = "",
    val note: String? = null,
    val customDiscount: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): CartItem = CartItem(
        id = id,
        productId = productId,
        productName = productName,
        sku = sku,
        unitPrice = unitPrice,
        taxRate = taxRate,
        quantity = quantity,
        imageUrl = imageUrl,
        modifiers = if (modifiersRaw.isBlank()) emptyList() else modifiersRaw.split("||"),
        note = note,
        customDiscount = customDiscount
    )

    fun toUiModel(): PosUiCartItem = PosUiCartItem(
        id = id,
        productId = productId,
        productName = productName,
        sku = sku,
        unitPrice = unitPrice,
        taxRate = taxRate,
        quantity = quantity,
        imageUrl = imageUrl,
        modifiers = if (modifiersRaw.isBlank()) emptyList() else modifiersRaw.split("||"),
        note = note,
        customDiscount = customDiscount
    )

    companion object {
        fun fromDomain(item: CartItem): CartItemEntity = CartItemEntity(
            id = item.id,
            productId = item.productId,
            productName = item.productName,
            sku = item.sku,
            unitPrice = item.unitPrice,
            taxRate = item.taxRate,
            quantity = item.quantity,
            imageUrl = item.imageUrl,
            modifiersRaw = item.modifiers.joinToString("||"),
            note = item.note,
            customDiscount = item.customDiscount
        )
    }
}
