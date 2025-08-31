package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.dto.InvestmentTrackerDTO
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.model.enums.InvesteeType
import com.selfgrowthfund.sgf.model.enums.InvestmentStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InvestmentDao {

    // CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(investment: Investment)

    @Update
    suspend fun update(investment: Investment)

    @Query("DELETE FROM investments WHERE investmentId = :id")
    suspend fun delete(id: String)

    // Single Item
    @Query("SELECT * FROM investments WHERE investmentId = :id")
    suspend fun getById(id: String): Investment?

    @Query("SELECT investmentId FROM investments ORDER BY investmentId DESC LIMIT 1")
    suspend fun getLastInvestmentId(): String?

    // Collections
    @Query("SELECT * FROM investments ORDER BY investmentDate DESC")
    fun getAll(): Flow<List<Investment>>

    @Query("SELECT * FROM investments WHERE status = :status ORDER BY investmentDate DESC")
    fun getByStatus(status: String): Flow<List<Investment>>

    @Query("SELECT * FROM investments WHERE investeeType = :type ORDER BY investmentDate DESC")
    fun getByInvesteeType(type: String): Flow<List<Investment>>

    // Date-based
    @Query("""
        SELECT * FROM investments  
        WHERE date(returnDueDate) BETWEEN date(:startDate) AND date(:endDate)
        AND status = 'Active'
        ORDER BY returnDueDate ASC
    """)
    suspend fun getDueBetween(startDate: LocalDate, endDate: LocalDate): List<Investment>

    // Search
    @Query("""
        SELECT * FROM investments 
        WHERE investmentId LIKE '%' || :query || '%'
        OR investmentName LIKE '%' || :query || '%'
        OR investeeName LIKE '%' || :query || '%'
        ORDER BY investmentDate DESC
    """)
    suspend fun search(query: String): List<Investment>

    // Aggregates
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
""")
    suspend fun getInvestmentTrackerSummary(): List<InvestmentTrackerDTO>

    @Query("""
    SELECT SUM(amountReceived)
    FROM investment_returns
    WHERE strftime('%Y-%m', returnDate) = :month
""")
    suspend fun getMonthlyReturns(month: String): Double

    @Query("SELECT * FROM investments WHERE status = :status")
    fun getByStatus(status: InvestmentStatus): Flow<List<Investment>>

    @Query("SELECT * FROM investments WHERE investeeType = :type")
    fun getByInvesteeType(type: InvesteeType): Flow<List<Investment>>
}