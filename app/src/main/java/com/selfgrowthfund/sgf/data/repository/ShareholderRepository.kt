package com.selfgrowthfund.sgf.data.repository

import android.util.Log
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ShareholderRepository (Realtime Firestore <-> Room Sync)
 *
 * - Room is the single source of truth.
 * - Local writes mark isSynced = false and trigger realtimeSyncRepository.pushAllUnsynced().
 * - Firestore updates handled automatically by RealtimeSyncRepository global listener.
 */
@Singleton
class ShareholderRepository @Inject constructor(
    private val dao: ShareholderDao,
    private val dates: Dates,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    // ──────────────── Reactive streams ────────────────
    fun getAllShareholdersStream(): Flow<List<Shareholder>> = dao.getAllShareholdersFlow()
        .onEach { list ->
            Log.d("ShareholderRepo", "Emitted ${list.size} shareholders from Room")
        }

    fun getShareholderByIdStream(id: String): Flow<Shareholder?> = dao.getShareholderByIdFlow(id)

    // ──────────────── Direct DB access ────────────────
    suspend fun getAllShareholders(): List<Shareholder> = dao.getAll()
    suspend fun getShareholderById(id: String): Shareholder? = dao.getShareholderById(id)
    suspend fun getLastShareholderId(): String? = dao.getLastShareholderId()
    suspend fun searchShareholders(query: String): List<Shareholder> =
        dao.searchShareholders("%$query%")

    // ──────────────── Create / Update / Delete ────────────────
    suspend fun addShareholder(shareholder: Shareholder): Result<Unit> = try {
        val entry = shareholder.withTimestamps().copy(isSynced = false)
        dao.insertShareholder(entry)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateShareholder(shareholder: Shareholder): Result<Unit> = try {
        dao.update(
            shareholder.copy(
                updatedAt = Instant.ofEpochMilli(dates.now()),
                isSynced = false
            )
        )
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteShareholderById(id: String): Result<Unit> = try {
        dao.getShareholderById(id)?.let {
            dao.deleteShareholder(it)
            realtimeSyncRepository.pushAllUnsynced()
            Result.Success(Unit)
        } ?: Result.Error(Exception("Shareholder not found"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteShareholder(shareholder: Shareholder): Result<Unit> = try {
        dao.deleteShareholder(shareholder)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun insertShareholderWithRoleCheck(shareholder: Shareholder) {
        val safeRole = shareholder.role
        val entry = shareholder.copy(role = safeRole, isSynced = false)
        dao.insertShareholder(entry)
        realtimeSyncRepository.pushAllUnsynced()
    }

    // ──────────────── Helpers ────────────────
    private fun Shareholder.withTimestamps(): Shareholder =
        copy(
            createdAt = Instant.ofEpochMilli(dates.now()),
            updatedAt = Instant.ofEpochMilli(dates.now())
        )
}
