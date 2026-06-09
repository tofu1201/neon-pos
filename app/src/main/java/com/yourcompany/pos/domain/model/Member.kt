package com.yourcompany.pos.domain.model

data class Member(
    val phone: String,
    val name: String,
    val balance: Double = 0.0,
    val points: Int = 0,
    val discountRate: Double = 1.0,
    val nfcCardId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
