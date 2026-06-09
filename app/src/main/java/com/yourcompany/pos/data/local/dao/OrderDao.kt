package com.yourcompany.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.yourcompany.pos.data.local.entity.OrderEntity
import com.yourcompany.pos.data.local.entity.OrderLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("SELECT * FROM orders WHERE createdAt BETWEEN :startTime AND :endTime ORDER BY createdAt DESC")
    suspend fun getOrdersByTimeRange(startTime: Long, endTime: Long): List<OrderEntity>

    @Insert
    suspend fun insertOrderLines(lines: List<OrderLineEntity>)

    @Transaction
    suspend fun insertOrderWithLines(order: OrderEntity, lines: List<OrderLineEntity>): Long {
        val orderId = insertOrder(order)
        val insertedLines = lines.map { it.copy(orderId = orderId) }
        insertOrderLines(insertedLines)
        return orderId
    }

    @androidx.room.Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("DELETE FROM order_lines WHERE orderId = :orderId")
    suspend fun deleteOrderLines(orderId: Long)

    @Transaction
    suspend fun updateOrderWithLines(order: OrderEntity, lines: List<OrderLineEntity>) {
        updateOrder(order)
        deleteOrderLines(order.id)
        val updatedLines = lines.map { it.copy(orderId = order.id) }
        insertOrderLines(updatedLines)
    }

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE orderNo = :orderNo LIMIT 1")
    suspend fun getOrderByOrderNo(orderNo: String): OrderEntity?

    @Query("SELECT * FROM order_lines WHERE orderId = :orderId ORDER BY id ASC")
    suspend fun getOrderLines(orderId: Long): List<OrderLineEntity>

    @Query("UPDATE orders SET syncStatus = :syncStatus, remoteOrderId = :remoteOrderId, errorMessage = :errorMessage, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateSyncState(
        orderId: Long,
        syncStatus: Boolean,
        remoteOrderId: String?,
        errorMessage: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateStatus(orderId: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET paymentMethod = :paymentMethod, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updatePaymentMethod(orderId: Long, paymentMethod: String, updatedAt: Long = System.currentTimeMillis())
}
