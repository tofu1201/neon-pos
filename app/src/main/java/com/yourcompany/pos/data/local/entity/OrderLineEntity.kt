package com.yourcompany.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourcompany.pos.domain.model.OrderLine

@Entity(tableName = "order_lines")
data class OrderLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: Double,
    val note: String? = null,
    val modifiersRaw: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): OrderLine = OrderLine(
        id = id,
        orderId = orderId,
        productId = productId,
        productName = productName,
        sku = sku,
        quantity = quantity,
        unitPrice = unitPrice,
        taxRate = taxRate,
        note = note,
        modifiers = if (modifiersRaw.isBlank()) emptyList() else modifiersRaw.split("||")
    )

    companion object {
        fun fromDomain(line: OrderLine): OrderLineEntity = OrderLineEntity(
            id = line.id,
            orderId = line.orderId,
            productId = line.productId,
            productName = line.productName,
            sku = line.sku,
            quantity = line.quantity,
            unitPrice = line.unitPrice,
            taxRate = line.taxRate,
            note = line.note,
            modifiersRaw = line.modifiers.joinToString("||")
        )
    }
}
