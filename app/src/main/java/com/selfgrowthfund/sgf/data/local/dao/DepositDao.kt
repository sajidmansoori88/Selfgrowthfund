package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(deposit: Deposit)

    @Query("SELECT * FROM deposits ORDER BY paymentDate DESC")
    fun getAll(): Flow<List<Deposit>>

    @Query("SELECT * FROM deposits WHERE shareholderId = :id")
    fun getByShareholder(id: String): Flow<List<Deposit>>

    @Query("SELECT depositId FROM deposits ORDER BY depositId DESC LIMIT 1")
    suspend fun getLastId(): String?

    @Query("""
        SELECT SUM(totalAmount) 
        FROM deposits 
        WHERE strftime('%m-%Y', createdAt/1000, 'unixepoch') = :monthYear
    """)
    suspend fun getMonthlyTotal(monthYear: String): Double
}