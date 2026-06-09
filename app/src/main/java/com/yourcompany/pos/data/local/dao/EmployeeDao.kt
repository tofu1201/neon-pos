package com.yourcompany.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.yourcompany.pos.data.local.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE isActive = 1")
    fun observeAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE pin = :pin LIMIT 1")
    suspend fun getEmployeeByPin(pin: String): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Long): EmployeeEntity?

    @Upsert
    suspend fun upsert(employee: EmployeeEntity)

    @Query("SELECT COUNT(id) FROM employees")
    suspend fun getCount(): Int
}
