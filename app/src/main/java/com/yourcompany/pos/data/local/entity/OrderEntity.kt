package com.yourcompany.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourcompany.pos.domain.model.Order
import com.yourcompany.pos.domain.model.OrderStatus
import com.yourcompany.pos.domain.model.PaymentMethod

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNo: String,
    val totalAmount: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val globalDiscount: Double = 0.0,
    val orderType: String = "外帶",
    val tableNumber: String? = null,
    val memberPhone: String? = null,
    val paymentMethod: String,
    val status: String,
    val syncStatus: Boolean = false,
    val remoteOrderId: String? = null,
    val errorMessage: String? = null,
    val pickupNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Order = Order(
        id = id,
        orderNo = orderNo,
        totalAmount = totalAmount,
        taxAmount = taxAmount,
        discountAmount = discountAmount,
        globalDiscount = globalDiscount,
        orderType = orderType,
        tableNumber = tableNumber,
        memberPhone = memberPhone,
        paymentMethod = PaymentMethod.valueOf(paymentMethod),
        status = OrderStatus.valueOf(status),
        syncStatus = syncStatus,
        remoteOrderId = remoteOrderId,
        errorMessage = errorMessage,
        pickupNumber = pickupNumber,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(order: Order): OrderEntity = OrderEntity(
            id = order.id,
            orderNo = order.orderNo,
            totalAmount = order.totalAmount,
            taxAmount = order.taxAmount,
            discountAmount = order.discountAmount,
            globalDiscount = order.globalDiscount,
            orderType = order.orderType,
            tableNumber = order.tableNumber,
            memberPhone = order.memberPhone,
            paymentMethod = order.paymentMethod.name,
            status = order.status.name,
            syncStatus = order.syncStatus,
            remoteOrderId = order.remoteOrderId,
            errorMessage = order.errorMessage,
            pickupNumber = order.pickupNumber,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt
        )
    }
}
