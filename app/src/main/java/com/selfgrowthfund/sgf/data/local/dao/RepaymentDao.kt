package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface RepaymentDao {
    // CRUD Operations
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(repayment: Repayment)

    @Update
    suspend fun update(repayment: Repayment)

    @Delete
    suspend fun delete(repayment: Repayment)

    @Query("SELECT * FROM repayments WHERE repaymentId = :repaymentId")
    suspend fun getById(repaymentId: String): Repayment?

    // Borrowing-specific Queries
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
        SELECT 
            COUNT(*) as count,
            SUM(principalRepaid) as totalPrincipal,
            SUM(penaltyPaid) as totalPenalty
        FROM repayments 
        WHERE borrowId = :borrowId
    """)
    suspend fun getBorrowingRepaymentSummary(borrowId: String): BorrowingRepaymentSummary

    // Shareholder Reports
    @Query("""
        SELECT 
            strftime('%Y-%m', datetime(repaymentDate / 1000, 'unixepoch')) as monthYear,
            SUM(principalRepaid) as principal,
            SUM(penaltyPaid) as penalty,
            COUNT(*) as transactions
        FROM repayments
        WHERE shareholderName = :shareholderName
        GROUP BY monthYear
        ORDER BY monthYear DESC
    """)
    suspend fun getMonthlyShareholderReport(shareholderName: String): List<MonthlyRepaymentReport>

    // Late Payment Detection
    @Query("""
        SELECT r.* 
        FROM repayments r
        JOIN borrowings b ON r.borrowId = b.borrowId
        WHERE r.shareholderName = :shareholderName
        AND datetime(r.repaymentDate / 1000, 'unixepoch') > datetime(b.dueDate / 1000, 'unixepoch', '+45 days')
        ORDER BY r.repaymentDate DESC
    """)
    suspend fun getLateRepayments(shareholderName: String): List<Repayment>

    // Full Text Search
    @Query("""
        SELECT * FROM repayments 
        WHERE notes LIKE '%' || :query || '%'
        OR repaymentId LIKE '%' || :query || '%'
        ORDER BY repaymentDate DESC
        LIMIT 50
    """)
    suspend fun searchRepayments(query: String): List<Repayment>

    // Complex Query Result
    @Query("""
        SELECT 
            r.*,
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
        @ColumnInfo(name = "count") val count: Int,
        @ColumnInfo(name = "totalPrincipal") val totalPrincipal: Double?,
        @ColumnInfo(name = "totalPenalty") val totalPenalty: Double?
    )

    data class MonthlyRepaymentReport(
        @ColumnInfo(name = "monthYear") val monthYear: String,
        @ColumnInfo(name = "principal") val principal: Double?,
        @ColumnInfo(name = "penalty") val penalty: Double?,
        @ColumnInfo(name = "transactions") val transactions: Int
    )

    data class RepaymentWithBorrowingDetails(
        @Embedded val repayment: Repayment,
        @ColumnInfo(name = "totalBorrowed") val totalBorrowed: Double,
        @ColumnInfo(name = "borrowingDueDate") val borrowingDueDate: Date
    )
}
