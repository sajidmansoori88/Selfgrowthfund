package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.dto.InvestmentTrackerDTO
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.InvesteeType
import com.selfgrowthfund.sgf.model.enums.InvestmentStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InvestmentDao {

    // --- Insert / Update / Delete ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(investment: Investment)

    @Update
    suspend fun update(investment: Investment)

    @Query("DELETE FROM investments WHERE provisionalId = :id")
    suspend fun deleteByProvisionalId(id: String)

    // --- Single Item Lookup ---
    @Query("SELECT * FROM investments WHERE provisionalId = :id LIMIT 1")
    suspend fun getByProvisionalId(id: String): Investment?

    @Query("SELECT * FROM investments WHERE investmentId = :id LIMIT 1")
    suspend fun getByInvestmentId(id: String): Investment?

    // --- ID Management ---
    @Query("SELECT investmentId FROM investments WHERE investmentId IS NOT NULL ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastApprovedInvestmentId(): String?

    // --- Approval workflow ---
    @Query("""
        UPDATE investments
        SET investmentId = :newId,
            approval_status = :status,
            approved_by = :approvedBy,
            approval_notes = :notes,
            updated_at = :updatedAt
        WHERE provisionalId = :provisionalId
    """)
    suspend fun approveInvestment(
        provisionalId: String,
        newId: String,
        status: ApprovalStage = ApprovalStage.ADMIN_APPROVED,
        approvedBy: String?,
        notes: String?,
        updatedAt: LocalDate
    )

    @Query("""
        UPDATE investments
        SET approval_status = :status,
            approved_by = :approvedBy,
            approval_notes = :notes,
            updated_at = :updatedAt
        WHERE provisionalId = :provisionalId
    """)
    suspend fun updateApprovalStatus(
        provisionalId: String,
        status: ApprovalStage,
        approvedBy: String?,
        notes: String?,
        updatedAt: LocalDate
    ): Int

    // --- Collections ---
    @Query("SELECT * FROM investments ORDER BY investmentDate DESC")
    fun getAll(): Flow<List<Investment>>

    @Query("SELECT * FROM investments WHERE status = :status ORDER BY investmentDate DESC")
    fun getByStatus(status: InvestmentStatus): Flow<List<Investment>>

    @Query("SELECT * FROM investments WHERE investeeType = :type ORDER BY investmentDate DESC")
    fun getByInvesteeType(type: InvesteeType): Flow<List<Investment>>

    // --- Date-based ---
    @Query("""
        SELECT * FROM investments  
        WHERE date(returnDueDate) BETWEEN date(:startDate) AND date(:endDate)
          AND status = 'Active'
        ORDER BY returnDueDate ASC
    """)
    suspend fun getDueBetween(startDate: LocalDate, endDate: LocalDate): List<Investment>

    // --- Search ---
    @Query("""
        SELECT * FROM investments 
        WHERE (investmentId LIKE '%' || :query || '%')
           OR (investmentName LIKE '%' || :query || '%')
           OR (investeeName LIKE '%' || :query || '%')
        ORDER BY investmentDate DESC
    """)
    suspend fun search(query: String): List<Investment>

    // --- Aggregates ---
    @Query("SELECT SUM(amount) FROM investments WHERE status = 'Active'")
    suspend fun getTotalActiveAmount(): Double?

    @Query("SELECT COUNT(*) FROM investments WHERE status = 'Active'")
    suspend fun getActiveCount(): Int

    @Query("""
        SELECT COUNT(*) as count,
               SUM(amount) as totalInvested,
               SUM(expectedProfitAmount) as totalExpectedProfit
        FROM investments
        WHERE status = 'Active'
    """)
    suspend fun getInvestmentSummary(): InvestmentSummary

    data class InvestmentSummary(
        @ColumnInfo(name = "count") val count: Int,
        @ColumnInfo(name = "totalInvested") val totalInvested: Double?,
        @ColumnInfo(name = "totalExpectedProfit") val totalExpectedProfit: Double?
    )

    @Query("SELECT SUM(amount) FROM investments")
    suspend fun getTotalInvested(): Double

    @Query("SELECT COUNT(*) FROM investments WHERE status = 'Closed'")
    suspend fun getClosedCount(): Int

    @Query("SELECT COUNT(*) FROM investments WHERE status = 'Overdue'")
    suspend fun getOverdueCount(): Int

    @Query("SELECT SUM(amount) FROM investments WHERE status = 'Closed'")
    suspend fun getReturnsFromClosed(): Double

    @Query("SELECT SUM(amount) FROM investments WHERE status = 'WrittenOff'")
    suspend fun getWrittenOffAmount(): Double

    // --- Tracker Summary ---
    @Query("""
        SELECT 
            i.investmentId,
            i.investmentName,
            i.investeeName AS investorName,
            i.returnDueDate AS expectedReturnDate,
            i.expectedProfitPercent,
            r.returnDate AS actualReturnDate,
            r.actualProfitPercent
        FROM investments i
        LEFT JOIN investment_returns r ON i.investmentId = r.investmentId
        WHERE i.investmentId IS NOT NULL
    """)
    suspend fun getInvestmentTrackerSummary(): List<InvestmentTrackerDTO>

    // --- Monthly Returns ---
    @Query("""
        SELECT SUM(amountReceived)
        FROM investment_returns
        WHERE strftime('%Y-%m', returnDate) = :month
    """)
    suspend fun getMonthlyReturns(month: String): Double

    // --- Approval Summary ---
    @Query("SELECT COUNT(*) FROM investments WHERE approval_status = :status AND createdAt BETWEEN :start AND :end")
    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Int

    @Query("SELECT COUNT(*) FROM investments WHERE createdAt BETWEEN :start AND :end")
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int

    @Query("SELECT * FROM investments WHERE createdAt BETWEEN :start AND :end")
    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Investment>

    // --- Flows ---
    @Query("SELECT * FROM investments WHERE provisionalId = :id LIMIT 1")
    fun getByProvisionalIdFlow(id: String): Flow<Investment?>

    @Query("SELECT * FROM investments WHERE investmentId = :id LIMIT 1")
    fun getByInvestmentIdFlow(id: String): Flow<Investment?>

    @Query("SELECT * FROM investments ORDER BY investmentDate DESC")
    fun getAllInvestmentsFlow(): Flow<List<Investment>>
}
