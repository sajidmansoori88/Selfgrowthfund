package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.DepositEntry
import com.selfgrowthfund.sgf.model.enums.DepositStatus
import com.selfgrowthfund.sgf.model.enums.EntrySource

@Dao
interface DepositEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deposit: DepositEntry)

    @Update
    suspend fun update(deposit: DepositEntry)

    @Query("SELECT * FROM deposit_entries WHERE depositId = :depositId")
    suspend fun getById(depositId: String): DepositEntry?

    @Query("SELECT * FROM deposit_entries WHERE shareholderId = :shareholderId ORDER BY dueMonth DESC")
    suspend fun getForShareholder(shareholderId: String): List<DepositEntry>

    @Query("SELECT * FROM deposit_entries WHERE dueMonth = :dueMonth AND shareholderId = :shareholderId LIMIT 1")
    suspend fun getForMonth(shareholderId: String, dueMonth: String): DepositEntry?

    @Query("SELECT * FROM deposit_entries WHERE status = :status ORDER BY dueMonth ASC")
    suspend fun getByStatus(status: DepositStatus): List<DepositEntry>

    @Query("SELECT * FROM deposit_entries WHERE status = :status AND dueMonth < :cutoffMonth")
    suspend fun getOverduePending(status: DepositStatus, cutoffMonth: String): List<DepositEntry>

    @Query("SELECT depositId FROM deposit_entries ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastId(): String?

    @Query("SELECT * FROM deposit_entries WHERE entrySource = :source")
    suspend fun getEntriesBySource(source: EntrySource): List<DepositEntry>
}
