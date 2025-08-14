package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.DepositEntry
import com.selfgrowthfund.sgf.data.local.types.DepositStatus
import com.selfgrowthfund.sgf.data.local.types.PaymentStatus
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepositEntry(entry: DepositEntry)

    @Update
    suspend fun updateDepositEntry(entry: DepositEntry)

    @Delete
    suspend fun deleteDepositEntry(entry: DepositEntry)

    @Query("SELECT * FROM deposit_entries ORDER BY createdAt DESC")
    fun getAllDeposits(): Flow<List<DepositEntry>>

    @Query("SELECT * FROM deposit_entries WHERE shareholderId = :shareholderId ORDER BY createdAt DESC")
    fun getDepositsByShareholder(shareholderId: String): Flow<List<DepositEntry>>

    @Query("SELECT * FROM deposit_entries WHERE dueMonth = :dueMonth AND status = :status")
    fun getDepositsByMonthAndStatus(dueMonth: DueMonth, status: DepositStatus): Flow<List<DepositEntry>>

    @Query("""
        SELECT * FROM deposit_entries 
        WHERE dueMonth = :dueMonth AND paymentStatus = :paymentStatus 
        AND status = :status
    """)
    fun getDepositsByPaymentStatus(
        dueMonth: DueMonth,
        paymentStatus: PaymentStatus,
        status: DepositStatus
    ): Flow<List<DepositEntry>>

    @Query("SELECT * FROM deposit_entries WHERE depositId = :depositId LIMIT 1")
    suspend fun getDepositById(depositId: String): DepositEntry?

    @Query("SELECT * FROM deposit_entries WHERE isSynced = 0")
    suspend fun getUnsyncedDeposits(): List<DepositEntry>

    @Query("DELETE FROM deposit_entries")
    suspend fun clearAll()
}
