package com.yourcompany.pos.domain.repository

import com.yourcompany.pos.domain.model.Order
import com.yourcompany.pos.domain.model.OrderLine
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun createOrder(order: Order, lines: List<OrderLine>): Long
    suspend fun updateOrder(order: Order, lines: List<OrderLine>)
    fun observeOrders(): Flow<List<Order>>
    suspend fun getOrdersByTimeRange(startTime: Long, endTime: Long): List<Order>
    suspend fun getOrderById(orderId: Long): Order?
    suspend fun getOrderByOrderNo(orderNo: String): Order?
    suspend fun getOrderLines(orderId: Long): List<OrderLine>
    suspend fun updateSyncState(
        orderId: Long,
        syncStatus: Boolean,
        remoteOrderId: String?,
        errorMessage: String?
    )

    suspend fun updateStatus(orderId: Long, status: com.yourcompany.pos.domain.model.OrderStatus)
    suspend fun updatePaymentMethod(orderId: Long, paymentMethod: com.yourcompany.pos.domain.model.PaymentMethod)
}
