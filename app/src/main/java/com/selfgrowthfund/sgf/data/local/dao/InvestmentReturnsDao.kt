package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

@Dao
interface InvestmentReturnsDao {

    // ========== CRUD ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(investmentReturn: InvestmentReturns)

    @Update
    suspend fun updateReturn(investmentReturn: InvestmentReturns)

    @Delete
    suspend fun deleteReturn(investmentReturn: InvestmentReturns)

    @Query("DELETE FROM investment_returns WHERE returnId = :returnId")
    suspend fun deleteReturnById(returnId: String)

    // ========== Queries ==========
    @Query("SELECT * FROM investment_returns WHERE investmentId = :investmentId")
    fun getReturnsForInvestment(investmentId: String): Flow<List<InvestmentReturns>>

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
}