package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.DepositEntry
import com.selfgrowthfund.sgf.model.enums.DepositStatus
import com.selfgrowthfund.sgf.model.enums.EntrySource
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deposit: DepositEntry)

    @Update
    suspend fun update(deposit: DepositEntry)

    @Delete
    suspend fun delete(deposit: DepositEntry)

    @Query("SELECT * FROM deposit_entries WHERE depositId = :depositId")
    suspend fun getById(depositId: String): DepositEntry?

    @Query("SELECT * FROM deposit_entries WHERE shareholderId = :shareholderId ORDER BY dueMonth DESC")
    fun getForShareholder(shareholderId: String): Flow<List<DepositEntry>>

    @Query("SELECT * FROM deposit_entries WHERE dueMonth = :dueMonth AND shareholderId = :shareholderId LIMIT 1")
    suspend fun getForMonth(shareholderId: String, dueMonth: String): DepositEntry?

    @Query("SELECT * FROM deposit_entries WHERE status = :status ORDER BY dueMonth ASC")
    fun getByStatus(status: DepositStatus): Flow<List<DepositEntry>>

    @Query("SELECT * FROM deposit_entries WHERE status = :status AND dueMonth < :cutoffMonth")
    fun getOverduePending(status: DepositStatus, cutoffMonth: String): Flow<List<DepositEntry>>

    @Query("SELECT depositId FROM deposit_entries ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastId(): String?

    @Query("SELECT * FROM deposit_entries WHERE entrySource = :source")
    fun getEntriesBySource(source: EntrySource): Flow<List<DepositEntry>>

    @Query("SELECT COUNT(*) FROM deposit_entries WHERE status = :status")
    fun getEntryCountByStatus(status: DepositStatus): Flow<Int>
}
