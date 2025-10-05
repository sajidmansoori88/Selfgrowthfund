package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.dto.ActiveBorrowingDTO
import com.selfgrowthfund.sgf.data.local.dto.ClosedBorrowingDTO
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
@TypeConverters(AppTypeConverters::class)
interface BorrowingDao {

    /* Create Operations */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBorrowing(borrowing: Borrowing)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(borrowings: List<Borrowing>)

    /* Read Operations */
    @Query("SELECT * FROM borrowings ORDER BY applicationDate DESC")
    fun getAllBorrowings(): Flow<List<Borrowing>>

    @Query("SELECT * FROM borrowings WHERE borrowId = :borrowId")
    suspend fun getBorrowingById(borrowId: String): Borrowing?

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId ORDER BY applicationDate DESC")
    fun getBorrowingsByShareholder(shareholderId: String): Flow<List<Borrowing>>

    @Query("SELECT * FROM borrowings WHERE status = :status ORDER BY applicationDate DESC")
    fun getBorrowingsByStatus(status: String): Flow<List<Borrowing>>

    @Query("SELECT * FROM borrowings WHERE status IN (:statuses) ORDER BY applicationDate DESC")
    fun getBorrowingsByStatuses(statuses: List<BorrowingStatus>): Flow<List<Borrowing>>

    @Query("SELECT * FROM borrowings WHERE approvalStatus = :status")
    suspend fun getByApprovalStatus(status: ApprovalStage): List<Borrowing>
    /* Update Operations */
    @Update
    suspend fun updateBorrowing(borrowing: Borrowing)

    @Query("SELECT * FROM borrowings WHERE provisionalId = :provisionalId LIMIT 1")
    suspend fun getByProvisionalId(provisionalId: String): Borrowing?

        @Update
        suspend fun update(borrowing: Borrowing)

    @Query("UPDATE borrowings SET status = :newStatus WHERE borrowId = :borrowId")
    suspend fun updateStatus(borrowId: String, newStatus: BorrowingStatus)

    @Query("UPDATE borrowings SET status = :status, closedDate = :closedDate WHERE borrowId = :borrowId")
    suspend fun updateBorrowingStatus(
        borrowId: String,
        status: String,
        closedDate: LocalDate?
    )

    // --- Approval Workflow ---
    @Query("""
        UPDATE borrowings 
        SET approvalStatus = :status, 
            approvedBy = :approvedBy, 
            notes = :notes,
            updatedAt = :timestamp 
        WHERE borrowId = :provisionalId
    """)
    suspend fun updateApprovalStatus(
        provisionalId: String,
        status: ApprovalStage,
        approvedBy: String?,
        notes: String?,
        timestamp: Long
    )

    /* Delete Operations */
    @Delete
    suspend fun deleteBorrowing(borrowing: Borrowing)

    @Query("DELETE FROM borrowings WHERE borrowId = :borrowId")
    suspend fun deleteById(borrowId: String)

    /* Special Queries */
    @Query("SELECT COUNT(*) FROM borrowings WHERE shareholderId = :shareholderId AND status NOT IN (:excludedStatuses)")
    suspend fun getActiveLoanCount(
        shareholderId: String,
        excludedStatuses: List<BorrowingStatus> = BorrowingStatus.getClosed().toList()
    ): Int

    @Query("SELECT SUM(approvedAmount) FROM borrowings WHERE shareholderId = :shareholderId AND status = :status")
    suspend fun getTotalLoanAmountByStatus(
        shareholderId: String,
        status: BorrowingStatus = BorrowingStatus.ACTIVE
    ): Double

    @Query("SELECT * FROM borrowings WHERE dueDate < :currentDate AND status = :status")
    fun getOverdueBorrowings(
        currentDate: LocalDate,
        status: BorrowingStatus = BorrowingStatus.ACTIVE
    ): Flow<List<Borrowing>>

    /* ID Generation Helper */
    @Query("SELECT borrowId FROM borrowings ORDER BY borrowId DESC LIMIT 1")
    suspend fun getLastBorrowingId(): String?

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId AND status = :status")
    suspend fun getBorrowingsByShareholder(
        shareholderId: String,
        status: BorrowingStatus
    ): List<Borrowing>

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId ORDER BY applicationDate DESC")
    fun getBorrowingsByShareholderFlow(shareholderId: String): Flow<List<Borrowing>>

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId AND status = :status ORDER BY applicationDate DESC")
    suspend fun getBorrowingsByShareholderWithStatus(
        shareholderId: String,
        status: BorrowingStatus
    ): List<Borrowing>

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId")
    suspend fun getBorrowingsByShareholderList(shareholderId: String): List<Borrowing>

    @Query("SELECT SUM(approvedAmount) FROM borrowings WHERE shareholderId = :shareholderId AND status = 'ACTIVE'")
    fun getTotalActiveLoanAmount(shareholderId: String): Double

    @Query("SELECT SUM(approvedAmount) FROM borrowings WHERE shareholderId = :shareholderId AND status = 'CLOSED'")
    fun getTotalClosedLoanAmount(shareholderId: String): Double

    @Query("SELECT SUM(approvedAmount) FROM borrowings")
    suspend fun getTotalBorrowed(): Double

    @Query("SELECT COUNT(*) FROM borrowings WHERE status = 'ACTIVE'")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM borrowings WHERE status = 'CLOSED'")
    suspend fun getClosedCount(): Int

    @Query("SELECT COUNT(*) FROM borrowings WHERE dueDate < date('now') AND status = 'ACTIVE'")
    suspend fun getOverdueCount(): Int

    @Query("""
        SELECT 
            b.borrowId,
            b.shareholderName,
            b.approvedAmount,
            b.dueDate,
            COALESCE(SUM(r.principalRepaid), 0) AS totalPrincipalRepaid,
            COALESCE(SUM(r.penaltyPaid), 0) AS totalPenaltyPaid,
            COALESCE(SUM(r.penaltyDue), 0) AS totalPenaltyAccrued
        FROM borrowings b
        LEFT JOIN repayments r ON b.borrowId = r.borrowId
        WHERE b.status = 'ACTIVE'
        GROUP BY b.borrowId, b.shareholderName, b.approvedAmount, b.dueDate
    """)
    suspend fun getActiveBorrowingSummary(): List<ActiveBorrowingDTO>

    @Query("""
        SELECT 
            b.borrowId,
            b.shareholderName,
            b.approvedAmount,
            b.borrowStartDate,
            b.closedDate,
            COALESCE(SUM(r.penaltyPaid), 0) AS totalPenaltyPaid,
            COALESCE(SUM(r.principalRepaid), 0) AS totalPrincipalRepaid
        FROM borrowings b
        LEFT JOIN repayments r ON b.borrowId = r.borrowId
        WHERE b.status = 'COMPLETED'
        GROUP BY b.borrowId, b.shareholderName, b.approvedAmount, b.borrowStartDate, b.closedDate
    """)
    suspend fun getClosedBorrowingSummary(): List<ClosedBorrowingDTO>

    @Query("SELECT SUM(principalRepaid + penaltyPaid) FROM repayments")
    suspend fun getTotalRepaid(): Double

    @Query("""
        SELECT SUM(approvedAmount) - COALESCE(SUM(r.principalRepaid), 0)
        FROM borrowings b
        LEFT JOIN repayments r ON b.borrowId = r.borrowId
        WHERE b.status = 'ACTIVE'
    """)
    suspend fun getOutstandingAmount(): Double

    @Query("""
        SELECT SUM(principalRepaid + penaltyPaid)
        FROM repayments
        WHERE strftime('%Y-%m', repaymentDate) = :month
    """)
    suspend fun getMonthlyRepayments(month: String): Double

    // --- Approval Queries ---
    @Query("SELECT COUNT(*) FROM borrowings WHERE approvalStatus = :status AND createdAt BETWEEN :start AND :end")
    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Int

    @Query("SELECT COUNT(*) FROM borrowings WHERE createdAt BETWEEN :start AND :end")
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int

    @Query("SELECT * FROM borrowings WHERE createdAt BETWEEN :start AND :end")
    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Borrowing>

    @Query("SELECT * FROM borrowings WHERE borrowId = :borrowingId LIMIT 1")
    suspend fun findById(borrowingId: String): Borrowing?
}
