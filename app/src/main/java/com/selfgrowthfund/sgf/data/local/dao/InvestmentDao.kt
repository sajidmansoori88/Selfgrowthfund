package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Investment
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

@Dao
interface InvestmentDao {

    // CRUD Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(investment: Investment)

    @Update
    suspend fun update(investment: Investment)

    @Query("DELETE FROM investments WHERE investmentId = :id")
    suspend fun delete(id: String)

    // Single Item Queries
    @Query("SELECT * FROM investments WHERE investmentId = :id")
    suspend fun getById(id: String): Investment?

    @Query("SELECT * FROM investments WHERE investmentId = :id LIMIT 1")
    suspend fun getInvestmentById(id: String): Investment?

    // Collection Queries
    @Query("SELECT * FROM investments ORDER BY investmentDate DESC")
    fun getAll(): Flow<List<Investment>>

    @Query("SELECT * FROM investments WHERE status = :status ORDER BY investmentDate DESC")
    fun getByStatus(status: String): Flow<List<Investment>>

    @Query("SELECT * FROM investments WHERE investeeType = :type ORDER BY investmentDate DESC")
    fun getByInvesteeType(type: String): Flow<List<Investment>>

    // Special Queries
    @Query("""
        SELECT * FROM investments 
        WHERE date(returnDueDate) BETWEEN date(:startDate) AND date(:endDate)
        AND status = 'Active'
        ORDER BY returnDueDate ASC
    """)
    suspend fun getDueBetween(startDate: LocalDate, endDate: LocalDate): List<Investment>

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
}