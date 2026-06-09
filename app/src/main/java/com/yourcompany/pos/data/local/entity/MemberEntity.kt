package com.yourcompany.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val phone: String, // Phone number as primary key
    val name: String,
    val balance: Double = 0.0,
    val points: Int = 0,
    val discountRate: Double = 1.0,
    val nfcCardId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object
}
