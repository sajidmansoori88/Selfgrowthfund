package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.BorrowingDao
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

class BorrowingRepository @Inject constructor(
    private val borrowingDao: BorrowingDao,
    private val shareholderDao: ShareholderDao,
    private val dates: Dates
) {
    // ==================== CRUD Operations ====================
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

    // ==================== Query Operations ====================
    fun getAllBorrowings(): Flow<List<Borrowing>> = borrowingDao.getAllBorrowings()

    // FIXED: Return type matches signature - returns Borrowing directly
    suspend fun getBorrowingById(borrowId: String): Borrowing {
        return borrowingDao.getBorrowingById(borrowId)
            ?: throw Exception("Borrowing not found with ID: $borrowId")
    }

    // FIXED: Added method that returns Result<Borrowing> if needed elsewhere
    suspend fun getBorrowingByIdWithResult(borrowId: String): Result<Borrowing> = try {
        val borrowing = borrowingDao.getBorrowingById(borrowId)
            ?: throw Exception("Borrowing not found with ID: $borrowId")
        Result.Success(borrowing)
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getBorrowingsByShareholder(shareholderId: String): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByShareholder(shareholderId)

    // FIXED: Use BorrowingStatus enum instead of String
    fun getBorrowingsByStatus(status: BorrowingStatus): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByStatus(status.name) // Convert enum to string for DAO

    // ==================== Status Management ====================
    // FIXED: Use BorrowingStatus enum instead of String
    suspend fun updateBorrowingStatus(
        borrowId: String,
        status: BorrowingStatus
    ): Result<Unit> = try {
        val closedDate: LocalDate? = if (BorrowingStatus.getClosedStatuses().contains(status)) {
            dates.today()
        } else {
            null
        }
        // FIXED: Pass status.name to convert enum to string for DAO
        borrowingDao.updateBorrowingStatus(borrowId, status.name, closedDate)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ==================== Business Logic ====================
    fun getOverdueBorrowings(): Flow<List<Borrowing>> {
        val today = dates.today()
        return borrowingDao.getOverdueBorrowings(today)
    }

    suspend fun getActiveLoanCount(shareholderId: String): Result<Int> = try {
        Result.Success(borrowingDao.getActiveLoanCount(shareholderId))
    } catch (e: Exception) {
        Result.Error(e)
    }

    // FIXED: Added proper implementation or handle missing method
    suspend fun getTotalActiveLoanAmount(shareholderId: String): Result<Double> = try {
        // Option 1: If the method exists in DAO
        Result.Success(borrowingDao.getTotalActiveLoanAmount(shareholderId))

        // Option 2: If method doesn't exist, calculate manually
        // val activeBorrowings = borrowingDao.getBorrowingsByStatus(BorrowingStatus.ACTIVE.name)
        // val total = activeBorrowings.sumOf { it.amount }
        // Result.Success(total)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ==================== ID Generation ====================
    private suspend fun generateNextBorrowingId(): String {
        val lastId = borrowingDao.getLastBorrowingId()
        val numeric = lastId?.removePrefix("BR")?.toIntOrNull() ?: 0
        return "BR" + String.format(Locale.US, "%04d", numeric + 1)
    }
    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        borrowingDao.countByStatus("APPROVED", start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        borrowingDao.countByStatus("REJECTED", start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        borrowingDao.countByStatus("PENDING", start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        borrowingDao.countTotal(start, end)
}