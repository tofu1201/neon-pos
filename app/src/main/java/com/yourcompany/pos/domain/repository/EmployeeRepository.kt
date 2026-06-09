package com.yourcompany.pos.domain.repository

import com.yourcompany.pos.domain.model.Employee
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    fun observeEmployees(): Flow<List<Employee>>
    suspend fun login(pin: String): Employee?
    suspend fun upsertEmployee(employee: Employee)
    suspend fun seedDefaultAdminIfEmpty()
}
