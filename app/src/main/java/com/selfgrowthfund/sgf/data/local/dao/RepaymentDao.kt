package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RepaymentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(repayment: Repayment)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(repayments: List<Repayment>)

    @Update
    suspend fun update(repayment: Repayment)

    @Delete
    suspend fun delete(repayment: Repayment)

    @Query("DELETE FROM repayments WHERE repaymentId = :repaymentId")
    suspend fun deleteById(repaymentId: String)

    @Query("SELECT * FROM repayments ORDER BY repaymentDate DESC")
    fun getAllRepayments(): Flow<List<Repayment>>

    @Query("SELECT * FROM repayments WHERE repaymentId = :repaymentId")
    suspend fun getById(repaymentId: String): Repayment?

    @Query("SELECT * FROM repayments WHERE borrowId = :borrowId ORDER BY repaymentDate DESC")
    fun getByBorrowId(borrowId: String): Flow<List<Repayment>>

    @Query("SELECT * FROM repayments WHERE borrowId = :borrowId ORDER BY repaymentDate DESC")
    suspend fun getByBorrowIdList(borrowId: String): List<Repayment>

    @Query("SELECT * FROM repayments WHERE borrowId = :borrowId ORDER BY repaymentDate DESC LIMIT 1")
    suspend fun getLastRepayment(borrowId: String): Repayment?

    @Query("UPDATE repayments SET borrowingStatus = :status WHERE repaymentId = :repaymentId")
    suspend fun updateStatus(repaymentId: String, status: String)

    @Query("SELECT repaymentId FROM repayments ORDER BY repaymentId DESC LIMIT 1")
    suspend fun getLastRepaymentId(): String?

    @Query("SELECT SUM(principalRepaid) FROM repayments WHERE borrowId = :borrowId")
    suspend fun getTotalPrincipalRepaid(borrowId: String): Double

    @Query("SELECT SUM(penaltyPaid) FROM repayments WHERE borrowId = :borrowId")
    suspend fun getTotalPenaltyPaid(borrowId: String): Double

    @Query("""
        SELECT COUNT(*) as count,
               SUM(principalRepaid) as totalPrincipal,
               SUM(penaltyPaid) as totalPenalty
        FROM repayments
        WHERE borrowId = :borrowId
    """)
    suspend fun getBorrowingRepaymentSummary(borrowId: String): BorrowingRepaymentSummary

    @Query("""
        SELECT r.*
        FROM repayments r
        JOIN borrowings b ON r.borrowId = b.borrowId
        WHERE r.repaymentDate > date(b.dueDate, '+45 days')
        ORDER BY r.repaymentDate DESC
    """)
    suspend fun getLateRepayments(): List<Repayment>

    @Query("""
        SELECT * FROM repayments
        WHERE notes LIKE '%' || :query || '%'
        OR repaymentId LIKE '%' || :query || '%'
        ORDER BY repaymentDate DESC
        LIMIT 50
    """)
    suspend fun searchRepayments(query: String): List<Repayment>

    data class BorrowingRepaymentSummary(
        @ColumnInfo(name = "count") val count: Int,
        @ColumnInfo(name = "totalPrincipal") val totalPrincipal: Double?,
        @ColumnInfo(name = "totalPenalty") val totalPenalty: Double?
    )
    // Total repayments received across all borrowings
    @Query("SELECT SUM(principalRepaid) FROM repayments")
    suspend fun getTotalPrincipalRepaidAll(): Double

    // Optional: if you consider penalties part of total received
    @Query("SELECT SUM(principalRepaid + penaltyPaid) FROM repayments")
    suspend fun getTotalRepaidAll(): Double
    @Query("""
    SELECT strftime('%Y-%m', repaymentDate) AS month, SUM(principalRepaid + penaltyPaid) AS total
    FROM repayments
    GROUP BY month
    ORDER BY month ASC
""")
    suspend fun getMonthlyRepayments(): List<MonthlyAmount>

    @Query("""
    SELECT SUM(principalRepaid + penaltyPaid)
    FROM repayments
    WHERE strftime('%Y-%m', repaymentDate) = :month
""")
    suspend fun getMonthlyRepayments(month: String): Double
}