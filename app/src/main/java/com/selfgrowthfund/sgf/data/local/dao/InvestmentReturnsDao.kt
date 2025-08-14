package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface InvestmentReturnsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(investmentReturn: InvestmentReturns)

    @Update
    suspend fun updateReturn(investmentReturn: InvestmentReturns)

    @Query("SELECT * FROM investment_returns WHERE investmentId = :investmentId")
    fun getReturnsForInvestment(investmentId: String): Flow<List<InvestmentReturns>>

    @Query("SELECT * FROM investment_returns")
    fun getAllReturns(): Flow<List<InvestmentReturns>>

    @Query("SELECT * FROM investment_returns WHERE returnId = :returnId")
    suspend fun getReturnById(returnId: String): InvestmentReturns?

    @Query("DELETE FROM investment_returns WHERE returnId = :returnId")
    suspend fun deleteReturn(returnId: String)

    @Query("SELECT * FROM investment_returns WHERE returnDate BETWEEN :start AND :end")
    fun getReturnsBetween(start: Date, end: Date): Flow<List<InvestmentReturns>>

    @Query("SELECT * FROM investment_returns WHERE investmentId = :investmentId ORDER BY returnDate DESC LIMIT 1")
    suspend fun getLatestReturn(investmentId: String): InvestmentReturns?
}