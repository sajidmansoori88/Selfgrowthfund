package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
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
}