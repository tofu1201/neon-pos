package com.yourcompany.pos.data.repository

import com.yourcompany.pos.data.local.dao.MemberDao
import com.yourcompany.pos.data.local.entity.MemberEntity
import com.yourcompany.pos.domain.model.Member
import com.yourcompany.pos.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemberRepositoryImpl(
    private val memberDao: MemberDao
) : MemberRepository {

    override suspend fun getMemberByPhone(phone: String): Member? {
        return memberDao.getMemberByPhone(phone)?.toDomain()
    }

    override suspend fun getMemberByNfcCardId(nfcCardId: String): Member? {
        return memberDao.getMemberByNfcCardId(nfcCardId)?.toDomain()
    }

    override fun observeAllMembers(): Flow<List<Member>> {
        return memberDao.observeAllMembers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertMember(member: Member) {
        memberDao.upsert(MemberEntity.fromDomain(member))
    }

    override suspend fun updateBalance(phone: String, amountDelta: Double): Boolean {
        val entity = memberDao.getMemberByPhone(phone) ?: return false
        val newBalance = entity.balance + amountDelta
        if (newBalance < 0) return false // Insufficient funds
        memberDao.upsert(entity.copy(balance = newBalance))
        return true
    }
}

// Extension functions for mapping
fun MemberEntity.toDomain() = Member(
    phone = phone,
    name = name,
    balance = balance,
    points = points,
    discountRate = discountRate,
    nfcCardId = nfcCardId,
    createdAt = createdAt
)

fun MemberEntity.Companion.fromDomain(member: Member) = MemberEntity(
    phone = member.phone,
    name = member.name,
    balance = member.balance,
    points = member.points,
    discountRate = member.discountRate,
    nfcCardId = member.nfcCardId,
    createdAt = member.createdAt
)
