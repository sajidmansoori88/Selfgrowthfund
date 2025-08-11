package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ShareholderRepository @Inject constructor(
    private val dao: ShareholderDao,
    private val dates: Dates
) {

    // Reactive streams
    fun getAllShareholdersStream(): Flow<List<Shareholder>> = dao.getAllShareholdersFlow()
    fun getShareholderByIdStream(id: String): Flow<Shareholder?> = dao.getShareholderByIdFlow(id)

    // Direct access
    suspend fun getShareholderById(id: String): Shareholder? = dao.getShareholderById(id)

    suspend fun searchShareholders(query: String): List<Shareholder> =
        dao.searchShareholders("%$query%")

    suspend fun addShareholder(shareholder: Shareholder): Result<Unit> = try {
        dao.insertShareholder(shareholder.withTimestamps())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateShareholder(shareholder: Shareholder): Result<Unit> = try {
        dao.updateShareholder(shareholder.copy(updatedAt = dates.now()))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteShareholderById(id: String): Result<Unit> = try {
        dao.getShareholderById(id)?.let {
            dao.deleteShareholder(it)
            Result.Success(Unit)
        } ?: Result.Error(Exception("Shareholder not found"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getLastShareholderId(): String? = dao.getLastId()

    // Helper
    private fun Shareholder.withTimestamps(): Shareholder =
        copy(createdAt = dates.now(), updatedAt = dates.now())
}