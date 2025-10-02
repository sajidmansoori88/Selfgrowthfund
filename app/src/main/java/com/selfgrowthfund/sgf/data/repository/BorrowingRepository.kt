package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.BorrowingDao
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
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

    suspend fun getBorrowingById(borrowId: String): Borrowing {
        return borrowingDao.getBorrowingById(borrowId)
            ?: throw Exception("Borrowing not found with ID: $borrowId")
    }

    suspend fun getBorrowingByIdWithResult(borrowId: String): Result<Borrowing> = try {
        val borrowing = borrowingDao.getBorrowingById(borrowId)
            ?: throw Exception("Borrowing not found with ID: $borrowId")
        Result.Success(borrowing)
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getBorrowingsByShareholder(shareholderId: String): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByShareholder(shareholderId)

    fun getBorrowingsByStatus(status: BorrowingStatus): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByStatus(status.name)

    // ==================== Status Management ====================
    suspend fun updateBorrowingStatus(
        borrowId: String,
        status: BorrowingStatus
    ): Result<Unit> = try {
        val closedDate: LocalDate? = if (BorrowingStatus.getClosed().contains(status)) {
            dates.today()
        } else null
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

    suspend fun getTotalActiveLoanAmount(shareholderId: String): Result<Double> = try {
        Result.Success(borrowingDao.getTotalActiveLoanAmount(shareholderId))
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ==================== Approval Flow ====================
    suspend fun approve(provisionalId: String, approverId: String? = null, notes: String? = null) {
        borrowingDao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.APPROVED,
            approvedBy = approverId,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun reject(provisionalId: String, rejectedBy: String? = null, notes: String? = null) {
        borrowingDao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.REJECTED,
            approvedBy = rejectedBy,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        borrowingDao.countByStatus(ApprovalStage.APPROVED, start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        borrowingDao.countByStatus(ApprovalStage.REJECTED, start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        borrowingDao.countByStatus(ApprovalStage.PENDING, start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        borrowingDao.countTotal(start, end)

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Borrowing> =
        borrowingDao.getApprovalsBetween(start, end)

    suspend fun findById(borrowingId: String) = borrowingDao.findById(borrowingId)

    // ==================== ID Generation ====================
    private suspend fun generateNextBorrowingId(): String {
        val lastId = borrowingDao.getLastBorrowingId()
        val numeric = lastId?.removePrefix("BR")?.toIntOrNull() ?: 0
        return "BR" + String.format(Locale.US, "%04d", numeric + 1)
    }
}
