// ShareholderEntryDao.kt
package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ShareholderEntryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ShareholderEntry)

    @Update
    suspend fun update(entry: ShareholderEntry)

    @Delete
    suspend fun delete(entry: ShareholderEntry)

    @Query("SELECT * FROM shareholder_entries ORDER BY entryId DESC")
    fun getAll(): Flow<List<ShareholderEntry>>

    @Query("SELECT * FROM shareholder_entries WHERE entryId = :id")
    suspend fun getById(id: Long): ShareholderEntry?

    @Query("DELETE FROM shareholder_entries WHERE entryId = :id")
    suspend fun deleteById(id: Long)

    // --- 🔁 SYNC HELPERS ---
    @Query("SELECT * FROM shareholder_entries WHERE isSynced = 0")
    suspend fun getUnsynced(): List<ShareholderEntry>

    @Query("SELECT * FROM shareholder_entries ORDER BY entryId DESC")
    suspend fun getAllEntriesList(): List<ShareholderEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ShareholderEntry>)
}