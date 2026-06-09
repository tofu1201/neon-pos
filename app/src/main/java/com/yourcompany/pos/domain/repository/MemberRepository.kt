package com.yourcompany.pos.domain.repository

import com.yourcompany.pos.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    suspend fun getMemberByPhone(phone: String): Member?
    suspend fun getMemberByNfcCardId(nfcCardId: String): Member?
    fun observeAllMembers(): Flow<List<Member>>
    suspend fun upsertMember(member: Member)
    suspend fun updateBalance(phone: String, amountDelta: Double): Boolean
}
