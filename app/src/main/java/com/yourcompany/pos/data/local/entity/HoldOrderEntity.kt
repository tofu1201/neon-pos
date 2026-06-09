package com.yourcompany.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hold_orders")
data class HoldOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val itemsJson: String,
    val totalAmount: Double,
    val note: String? = null
)
