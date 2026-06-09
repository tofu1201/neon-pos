package com.yourcompany.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourcompany.pos.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings")
    fun observeAllSettings(): Flow<List<SettingsEntity>>

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsEntity>

    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: SettingsEntity)
}
