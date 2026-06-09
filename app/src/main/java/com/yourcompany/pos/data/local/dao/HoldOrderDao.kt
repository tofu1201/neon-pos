package com.yourcompany.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yourcompany.pos.data.local.entity.HoldOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HoldOrderDao {
    @Query("SELECT * FROM hold_orders ORDER BY timestamp DESC")
    fun getAllHoldOrders(): Flow<List<HoldOrderEntity>>

    @Insert
    suspend fun insertHoldOrder(holdOrder: HoldOrderEntity): Long

    @Query("DELETE FROM hold_orders WHERE id = :id")
    suspend fun deleteHoldOrder(id: Long)
}
