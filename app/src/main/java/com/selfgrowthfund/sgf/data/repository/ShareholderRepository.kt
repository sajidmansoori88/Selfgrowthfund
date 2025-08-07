// app/src/main/java/com/selfgrowthfund/sgf/data/repository/ShareholderRepositoryImpl.kt
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
    // Flow for reactive updates
    fun getAllShareholdersStream(): Flow<List<Shareholder>> = dao.getAllShareholdersFlow()

    // Standard suspend functions
    suspend fun getShareholderById(id: String): Shareholder? = dao.getShareholderById(id)

    suspend fun searchShareholders(query: String): List<Shareholder> =
        dao.searchShareholders("%$query%")

    suspend fun addShareholder(shareholder: Shareholder): Result<Unit> = try {
        shareholder.apply {
            createdAt = dates.now()
            updatedAt = createdAt
        }
        dao.insertShareholder(shareholder)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateShareholder(shareholder: Shareholder): Result<Unit> = try {
        shareholder.updatedAt = dates.now()
        dao.updateShareholder(shareholder)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteShareholder(id: String): Result<Unit> = try {
        dao.deleteShareholder(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}