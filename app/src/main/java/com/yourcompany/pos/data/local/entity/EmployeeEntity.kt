package com.yourcompany.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourcompany.pos.domain.model.Employee
import com.yourcompany.pos.domain.model.EmployeeRole

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pin: String,
    val role: String,
    val isActive: Boolean = true
) {
    fun toDomain(): Employee = Employee(
        id = id,
        name = name,
        pin = pin,
        role = try { EmployeeRole.valueOf(role) } catch (e: Exception) { EmployeeRole.CASHIER },
        isActive = isActive
    )

    companion object {
        fun fromDomain(employee: Employee): EmployeeEntity = EmployeeEntity(
            id = employee.id,
            name = employee.name,
            pin = employee.pin,
            role = employee.role.name,
            isActive = employee.isActive
        )
    }
}
