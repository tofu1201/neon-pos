package com.yourcompany.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.yourcompany.pos.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE phone = :phone LIMIT 1")
    suspend fun getMemberByPhone(phone: String): MemberEntity?

    @Query("SELECT * FROM members WHERE nfcCardId = :nfcCardId LIMIT 1")
    suspend fun getMemberByNfcCardId(nfcCardId: String): MemberEntity?

    @Query("SELECT * FROM members ORDER BY createdAt DESC")
    fun observeAllMembers(): Flow<List<MemberEntity>>

    @Upsert
    suspend fun upsert(member: MemberEntity)
}
