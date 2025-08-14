package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface InvestmentReturnsDao {

    // Insert or replace a return
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(investmentReturn: InvestmentReturns)

    // Get all returns as a Flow
    @Query("SELECT * FROM investment_returns")
    fun getAll(): Flow<List<InvestmentReturns>>

    // Get returns for a specific investment
    @Query("SELECT * FROM investment_returns WHERE investmentId = :id")
    fun getByInvestmentId(id: String): Flow<List<InvestmentReturns>>

    // Get returns between two LocalDateTime values
    @Query("SELECT * FROM investment_returns WHERE returnDate BETWEEN :start AND :end")
    fun getByDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<InvestmentReturns>>

    // Update a return
    @Update
    suspend fun update(investmentReturn: InvestmentReturns)

    // Delete a return by entity
    @Delete
    suspend fun delete(investmentReturn: InvestmentReturns)

    // Optional: Delete a return by ID
    @Query("DELETE FROM investment_returns WHERE returnId = :id")
    suspend fun deleteById(id: String)
}
