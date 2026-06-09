package com.yourcompany.pos.data.repository

import com.yourcompany.pos.data.local.dao.EmployeeDao
import com.yourcompany.pos.data.local.entity.EmployeeEntity
import com.yourcompany.pos.domain.model.Employee
import com.yourcompany.pos.domain.model.EmployeeRole
import com.yourcompany.pos.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EmployeeRepositoryImpl(
    private val employeeDao: EmployeeDao
) : EmployeeRepository {
    override fun observeEmployees(): Flow<List<Employee>> =
        employeeDao.observeAllEmployees().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun login(pin: String): Employee? {
        return employeeDao.getEmployeeByPin(pin)?.toDomain()
    }

    override suspend fun upsertEmployee(employee: Employee) {
        employeeDao.upsert(EmployeeEntity.fromDomain(employee))
    }

    override suspend fun seedDefaultAdminIfEmpty() {
        if (employeeDao.getCount() == 0) {
            employeeDao.upsert(
                EmployeeEntity(
                    name = "店長",
                    pin = "1234",
                    role = EmployeeRole.ADMIN.name
                )
            )
        }
    }
}
