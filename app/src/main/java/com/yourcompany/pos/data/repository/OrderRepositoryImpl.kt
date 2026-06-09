package com.yourcompany.pos.data.repository

import com.yourcompany.pos.data.local.dao.OrderDao
import com.yourcompany.pos.data.local.entity.OrderEntity
import com.yourcompany.pos.data.local.entity.OrderLineEntity
import com.yourcompany.pos.domain.model.Order
import com.yourcompany.pos.domain.model.OrderLine
import com.yourcompany.pos.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepositoryImpl(
    private val orderDao: OrderDao
) : OrderRepository {
    override suspend fun createOrder(order: Order, lines: List<OrderLine>): Long {
        return orderDao.insertOrderWithLines(
            OrderEntity.fromDomain(order),
            lines.map(OrderLineEntity::fromDomain)
        )
    }

    override suspend fun updateOrder(order: Order, lines: List<OrderLine>) {
        orderDao.updateOrderWithLines(
            OrderEntity.fromDomain(order),
            lines.map(OrderLineEntity::fromDomain)
        )
    }

    override fun observeOrders(): Flow<List<Order>> =
        orderDao.observeOrders().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getOrdersByTimeRange(startTime: Long, endTime: Long): List<Order> {
        return orderDao.getOrdersByTimeRange(startTime, endTime).map { entity ->
            Order(
                id = entity.id,
                orderNo = entity.orderNo,
                totalAmount = entity.totalAmount,
                taxAmount = entity.taxAmount,
                discountAmount = entity.discountAmount,
                globalDiscount = entity.globalDiscount,
                paymentMethod = com.yourcompany.pos.domain.model.PaymentMethod.valueOf(entity.paymentMethod),
                status = com.yourcompany.pos.domain.model.OrderStatus.valueOf(entity.status),
                syncStatus = entity.syncStatus,
                createdAt = entity.createdAt,
                orderType = entity.orderType,
                tableNumber = entity.tableNumber
            )
        }
    }

    override suspend fun getOrderById(orderId: Long): Order? {
        return orderDao.getOrderById(orderId)?.toDomain()
    }

    override suspend fun getOrderByOrderNo(orderNo: String): Order? {
        return orderDao.getOrderByOrderNo(orderNo)?.toDomain()
    }

    override suspend fun getOrderLines(orderId: Long): List<OrderLine> =
        orderDao.getOrderLines(orderId).map { it.toDomain() }

    override suspend fun updateSyncState(
        orderId: Long,
        syncStatus: Boolean,
        remoteOrderId: String?,
        errorMessage: String?
    ) {
        orderDao.updateSyncState(
            orderId = orderId,
            syncStatus = syncStatus,
            remoteOrderId = remoteOrderId,
            errorMessage = errorMessage
        )
    }

    override suspend fun updateStatus(orderId: Long, status: com.yourcompany.pos.domain.model.OrderStatus) {
        orderDao.updateStatus(orderId, status.name)
    }

    override suspend fun updatePaymentMethod(orderId: Long, paymentMethod: com.yourcompany.pos.domain.model.PaymentMethod) {
        orderDao.updatePaymentMethod(orderId, paymentMethod.name)
    }
}
