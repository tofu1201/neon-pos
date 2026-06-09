package com.yourcompany.pos.data.repository

import com.yourcompany.pos.data.local.dao.SettingsDao
import com.yourcompany.pos.data.local.entity.SettingsEntity
import com.yourcompany.pos.domain.model.Setting
import com.yourcompany.pos.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(private val settingsDao: SettingsDao) : SettingsRepository {
    override fun observeAllSettings(): Flow<List<Setting>> = 
        settingsDao.observeAllSettings().map { entities ->
            entities.map { Setting(it.key, it.value) }
        }

    override suspend fun getAllSettings(): Map<String, String> {
        return settingsDao.getAllSettings().associate { it.key to it.value }
    }

    override suspend fun getSettingValue(key: String, defaultValue: String): String {
        return settingsDao.getSettingValue(key) ?: defaultValue
    }

    override suspend fun saveSetting(key: String, value: String) {
        settingsDao.saveSetting(SettingsEntity(key = key, value = value))
    }

    override suspend fun saveSettings(settings: Map<String, String>) {
        settings.forEach { (key, value) ->
            settingsDao.saveSetting(SettingsEntity(key = key, value = value))
        }
    }
}
