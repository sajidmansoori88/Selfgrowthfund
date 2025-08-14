package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.data.local.types.PaymentMode
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface RepaymentDao {
    // CRUD
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(repayment: Repayment)

    @Update
    suspend fun update(repayment: Repayment)

    @Delete
    suspend fun delete(repayment: Repayment)

    @Query("SELECT * FROM repayments WHERE repaymentId = :repaymentId")
    suspend fun getById(repaymentId: String): Repayment?

    // Borrowing-specific
    @Query("SELECT * FROM repayments WHERE borrowId = :borrowId ORDER BY repaymentDate DESC")
    suspend fun getByBorrowIdList(borrowId: String): List<Repayment>

    @Query("SELECT * FROM repayments WHERE borrowId = :borrowId ORDER BY repaymentDate DESC")
    fun getByBorrowIdFlow(borrowId: String): Flow<List<Repayment>>

    @Query("SELECT * FROM repayments WHERE borrowId = :borrowId ORDER BY repaymentDate DESC LIMIT 1")
    suspend fun getLastRepayment(borrowId: String): Repayment?

    // Aggregates
    @Query("SELECT SUM(principalRepaid) FROM repayments WHERE borrowId = :borrowId")
    suspend fun getTotalPrincipalRepaid(borrowId: String): Double

    @Query("SELECT SUM(penaltyPaid) FROM repayments WHERE borrowId = :borrowId")
    suspend fun getTotalPenaltyPaid(borrowId: String): Double

    @Query("""
        SELECT COUNT(*) as count,
               SUM(principalRepaid) as totalPrincipal,
               SUM(penaltyPaid) as totalPenalty
        FROM repayments WHERE borrowId = :borrowId
    """)
    suspend fun getBorrowingRepaymentSummary(borrowId: String): BorrowingRepaymentSummary

    // Payment Mode
    @Query("SELECT COUNT(*) FROM repayments WHERE modeOfPayment = :mode AND borrowId = :borrowId")
    suspend fun countByPaymentMode(borrowId: String, mode: PaymentMode): Int

    @Query("""
        SELECT SUM(totalAmountPaid) 
        FROM repayments 
        WHERE modeOfPayment = :mode 
        AND strftime('%Y-%m', repaymentDate) = :monthYear
    """)
    suspend fun getMonthlyTotalByPaymentMode(mode: PaymentMode, monthYear: String): Double?

    // Reports
    @Query("""
        SELECT strftime('%Y-%m', repaymentDate) as monthYear,
               SUM(principalRepaid) as principal,
               SUM(penaltyPaid) as penalty,
               COUNT(*) as transactions
        FROM repayments
        WHERE shareholderName = :shareholderName
        GROUP BY monthYear
        ORDER BY monthYear DESC
    """)
    suspend fun getMonthlyShareholderReport(shareholderName: String): List<MonthlyRepaymentReport>

    // Late payments
    @Query("""
        SELECT r.* FROM repayments r
        JOIN borrowings b ON r.borrowId = b.borrowId
        WHERE r.shareholderName = :shareholderName
        AND r.repaymentDate > datetime(b.dueDate, '+45 days')
        ORDER BY r.repaymentDate DESC
    """)
    suspend fun getLateRepayments(shareholderName: String): List<Repayment>

    // Search
    @Query("""
        SELECT * FROM repayments  
        WHERE notes LIKE '%' || :query || '%'
        OR repaymentId LIKE '%' || :query || '%'
        ORDER BY repaymentDate DESC
        LIMIT 50
    """)
    suspend fun searchRepayments(query: String): List<Repayment>

    // Detailed info
    @Query("""
        SELECT r.*,
               b.approvedAmount as totalBorrowed,
               b.dueDate as borrowingDueDate
        FROM repayments r
        JOIN borrowings b ON r.borrowId = b.borrowId
        WHERE r.borrowId = :borrowId
        ORDER BY r.repaymentDate DESC
    """)
    suspend fun getRepaymentsWithBorrowingDetails(borrowId: String): List<RepaymentWithBorrowingDetails>

    // DTOs
    data class BorrowingRepaymentSummary(
        val count: Int,
        val totalPrincipal: Double?,
        val totalPenalty: Double?
    )

    data class MonthlyRepaymentReport(
        val monthYear: String,
        val principal: Double?,
        val penalty: Double?,
        val transactions: Int
    )

    data class RepaymentWithBorrowingDetails(
        @Embedded val repayment: Repayment,
        val totalBorrowed: Double,
        val borrowingDueDate: LocalDateTime
    )
}