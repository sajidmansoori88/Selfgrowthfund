package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InvestmentReturnsDao {

    // ========== CRUD ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(returnEntity: InvestmentReturns)

    @Update
    suspend fun updateReturn(returnEntity: InvestmentReturns)

    @Delete
    suspend fun deleteReturn(returnEntity: InvestmentReturns)

    @Query("DELETE FROM investment_returns WHERE returnId = :returnId")
    suspend fun deleteReturnById(returnId: String)

    // ========== Queries ==========
    @Query("SELECT * FROM investment_returns WHERE investmentId = :investmentId")
    fun getReturnsByInvestmentId(investmentId: String): List<InvestmentReturns>

    @Query("SELECT * FROM investment_returns")
    fun getAllReturns(): Flow<List<InvestmentReturns>>

    @Query("SELECT * FROM investment_returns WHERE returnId = :returnId")
    suspend fun getReturnById(returnId: String): InvestmentReturns?

    @Query("""
        SELECT * FROM investment_returns 
        WHERE date(returnDate) BETWEEN date(:start) AND date(:end)
        ORDER BY returnDate ASC
    """)
    fun getReturnsBetween(start: LocalDate, end: LocalDate): Flow<List<InvestmentReturns>>

    @Query("""
        SELECT * FROM investment_returns 
        WHERE investmentId = :investmentId 
        ORDER BY returnDate DESC 
        LIMIT 1
    """)
    suspend fun getLatestReturn(investmentId: String): InvestmentReturns?

    // --- Monthly Reports ---
    @Query("""
        SELECT SUM(amountReceived)
        FROM investment_returns
        WHERE strftime('%Y-%m', returnDate) = :month
    """)
    suspend fun getMonthlyReturns(month: String): Double

    @Query("""
        SELECT strftime('%Y-%m', returnDate) AS month, SUM(amountReceived) AS total
        FROM investment_returns
        GROUP BY month
        ORDER BY month ASC
    """)
    suspend fun getMonthlyInvestmentTimeline(): List<MonthlyAmount>

    // --- Approval Workflow ---
    @Query("""
        UPDATE investment_returns
        SET approval_status = :status,
            approved_by = :approvedBy,
            approval_notes = :notes,
            updated_at = :updatedAt
        WHERE returnId = :returnId
    """)
    suspend fun updateApprovalStatus(
        returnId: String,
        status: ApprovalStage,
        approvedBy: String?,
        notes: String?,
        updatedAt: LocalDate
    ): Int

    @Query("SELECT COUNT(*) FROM investment_returns WHERE approval_status = :status AND createdAt BETWEEN :start AND :end")
    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Int

    @Query("SELECT COUNT(*) FROM investment_returns WHERE createdAt BETWEEN :start AND :end")
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int

    @Query("SELECT * FROM investment_returns WHERE createdAt BETWEEN :start AND :end")
    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<InvestmentReturns>

    @Query("SELECT * FROM investment_returns WHERE returnId = :returnsId LIMIT 1")
    suspend fun findById(returnsId: String): InvestmentReturns?

    // --- Flows ---
    @Query("SELECT * FROM investment_returns WHERE investmentId = :investmentId ORDER BY returnDate DESC")
    fun getReturnsByInvestmentIdFlow(investmentId: String): Flow<List<InvestmentReturns>>
}
