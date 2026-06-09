package com.yourcompany.pos.domain.repository

import com.yourcompany.pos.domain.model.Setting
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeAllSettings(): Flow<List<Setting>>
    suspend fun getAllSettings(): Map<String, String>
    suspend fun getSettingValue(key: String, defaultValue: String = ""): String
    suspend fun saveSetting(key: String, value: String)
    suspend fun saveSettings(settings: Map<String, String>)
}
