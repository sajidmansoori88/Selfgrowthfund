package com.selfgrowthfund.sgf.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.local.types.ShareholderStatus
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class ShareholderRepository @Inject constructor(
    private val dao: ShareholderDao
) {
    // ─────────────── Paging Configuration ───────────────
    private val pagingConfig = PagingConfig(
        pageSize = 20,
        enablePlaceholders = false,
        prefetchDistance = 5
    )

    // ─────────────── Reactive Streams ───────────────
    fun getAllShareholdersStream(): Flow<List<Shareholder>> =
        dao.getAllShareholdersFlow()

    fun getShareholderByIdStream(id: String): Flow<Shareholder?> =
        dao.getShareholderByIdFlow(id)

    fun getPagedShareholdersStream(): Flow<PagingData<Shareholder>> =
        Pager(
            config = pagingConfig,
            pagingSourceFactory = { dao.getPagedShareholders() }
        ).flow

    // ─────────────── Loan Status Results ───────────────
    data class ShareholderLoanStatus(
        val shareholder: Shareholder,
        val maxBorrowAmount: Double,
        val currentLoans: Double,
        val availableCredit: Double = maxBorrowAmount - currentLoans
    )

    // ─────────────── Suspend Operations ───────────────
    suspend fun getShareholderById(id: String): Shareholder? =
        dao.getShareholderById(id)

    suspend fun getLastShareholderId(): String? =
        dao.getLastId()

    suspend fun getLoanStatus(id: String): Result<ShareholderLoanStatus> =
        try {
            val result = dao.getLoanStatus(id)
            result?.let {
                Result.Success(
                    ShareholderLoanStatus(
                        shareholder = it.shareholder,
                        maxBorrowAmount = it.maxBorrowAmount,
                        currentLoans = it.currentLoans
                    )
                )
            } ?: Result.Error(NullPointerException("Shareholder not found"))
        } catch (e: Exception) {
            Result.Error(e)
        }

    // ─────────────── CRUD Operations ───────────────
    suspend fun addShareholder(shareholder: Shareholder): Result<Unit> =
        try {
            dao.insertShareholder(shareholder)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun updateShareholder(shareholder: Shareholder): Result<Unit> =
        try {
            dao.updateShareholder(shareholder)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun deleteShareholderById(id: String): Result<Unit> =
        try {
            dao.deleteById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    // ─────────────── Status Management ───────────────
    suspend fun activateShareholder(
        id: String,
        timestamp: LocalDateTime = LocalDateTime.now()
    ): Result<Unit> = try {
        dao.updateStatus(
            id = id,
            status = ShareholderStatus.Active,
            exitDate = null,
            timestamp = timestamp
        )
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deactivateShareholder(
        id: String,
        exitDate: LocalDateTime = LocalDateTime.now(),
        timestamp: LocalDateTime = LocalDateTime.now()
    ): Result<Unit> = try {
        dao.updateStatus(
            id = id,
            status = ShareholderStatus.Inactive,
            exitDate = exitDate,
            timestamp = timestamp
        )
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}