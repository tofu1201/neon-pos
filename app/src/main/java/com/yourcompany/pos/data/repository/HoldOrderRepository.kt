package com.yourcompany.pos.data.repository

import com.yourcompany.pos.data.local.dao.HoldOrderDao
import com.yourcompany.pos.data.local.entity.HoldOrderEntity
import kotlinx.coroutines.flow.Flow

class HoldOrderRepository(private val dao: HoldOrderDao) {
    fun getAllHoldOrders(): Flow<List<HoldOrderEntity>> = dao.getAllHoldOrders()

    suspend fun saveHoldOrder(itemsJson: String, totalAmount: Double, note: String?) {
        val entity = HoldOrderEntity(
            timestamp = System.currentTimeMillis(),
            itemsJson = itemsJson,
            totalAmount = totalAmount,
            note = note
        )
        dao.insertHoldOrder(entity)
    }

    suspend fun deleteHoldOrder(id: Long) {
        dao.deleteHoldOrder(id)
    }
}
