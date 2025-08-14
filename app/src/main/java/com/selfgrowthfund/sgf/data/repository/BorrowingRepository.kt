package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.BorrowingDao
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.data.local.types.BorrowingStatus
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

class BorrowingRepository @Inject constructor(
    private val borrowingDao: BorrowingDao,
    private val shareholderDao: ShareholderDao,
    private val dates: Dates
) {
    // ─────────────── CRUD Operations ───────────────
    suspend fun createBorrowing(borrowing: Borrowing): Result<String> = try {
        val newId = generateNextBorrowingId()
        val borrowingWithId = borrowing.copy(borrowId = newId)
        borrowingDao.insertBorrowing(borrowingWithId)
        Result.Success(newId)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateBorrowing(borrowing: Borrowing): Result<Unit> = try {
        borrowingDao.updateBorrowing(borrowing)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteBorrowing(borrowId: String): Result<Unit> = try {
        borrowingDao.deleteById(borrowId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ─────────────── Query Operations ───────────────
    fun getAllBorrowings(): Flow<List<Borrowing>> = borrowingDao.getAllBorrowings()

    suspend fun getBorrowingById(borrowId: String): Result<Borrowing> = try {
        val borrowing = borrowingDao.getBorrowingById(borrowId)
        if (borrowing != null) Result.Success(borrowing)
        else Result.Error(Exception("Borrowing not found"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getBorrowingsByShareholder(shareholderId: String): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByShareholder(shareholderId)

    fun getBorrowingsByStatus(status: BorrowingStatus): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByStatus(status.name)

    // ─────────────── Status Management ───────────────
    suspend fun updateBorrowingStatus(
        borrowId: String,
        status: BorrowingStatus
    ): Result<Unit> = try {
        val closedDate = if (status in BorrowingStatus.getClosedStatuses()) nowAsLocalDateTime() else null
        borrowingDao.updateBorrowingStatus(borrowId, status, closedDate)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ─────────────── Business Logic ───────────────
    fun getOverdueBorrowings(): Flow<List<Borrowing>> =
        borrowingDao.getOverdueBorrowings(nowAsLocalDateTime())

    suspend fun getActiveLoanCount(shareholderId: String): Result<Int> = try {
        Result.Success(borrowingDao.getActiveLoanCount(shareholderId))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getTotalActiveLoanAmount(shareholderId: String): Result<Double> = try {
        Result.Success(borrowingDao.getTotalActiveLoanAmount(shareholderId))
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ─────────────── ID Generation ───────────────
    private suspend fun generateNextBorrowingId(): String {
        val lastId = borrowingDao.getLastBorrowingId()
        val numeric = lastId?.removePrefix("BR")?.toIntOrNull() ?: 0
        return "BR" + String.format(Locale.US, "%04d", numeric + 1)
    }

    // ─────────────── Utility ───────────────
    private fun nowAsLocalDateTime(): LocalDateTime =
        dates.now().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}