package com.yourcompany.pos.domain.model

enum class EmployeeRole {
    ADMIN,
    CASHIER
}

data class Employee(
    val id: Long,
    val name: String,
    val pin: String,
    val role: EmployeeRole,
    val isActive: Boolean = true
)
