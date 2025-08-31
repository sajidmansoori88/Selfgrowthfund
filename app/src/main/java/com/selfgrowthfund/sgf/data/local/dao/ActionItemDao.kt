package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.ActionItem
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.model.enums.ActionResponse
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
@TypeConverters(AppTypeConverters::class)
interface ActionItemDao {

    // ─────────────── Create / Update / Delete ───────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ActionItem)

    @Update
    suspend fun updateAction(action: ActionItem)

    @Delete
    suspend fun deleteAction(action: ActionItem)

    // ─────────────── Single Item ───────────────
    @Query("SELECT * FROM action_items WHERE actionId = :actionId")
    suspend fun getActionById(actionId: String): ActionItem?

    // ─────────────── Streams (Flow) ───────────────
    @Query("SELECT * FROM action_items ORDER BY createdAt DESC")
    fun getAllActions(): Flow<List<ActionItem>>

    @Query("SELECT * FROM action_items WHERE deadline IS NULL OR deadline > :now")
    fun getPendingActions(now: LocalDateTime): Flow<List<ActionItem>>

    @Query("SELECT * FROM action_items WHERE createdBy = :shareholderId")
    fun getActionsCreatedBy(shareholderId: String): Flow<List<ActionItem>>

    @Query("SELECT * FROM action_items WHERE type = :typeLabel ORDER BY createdAt DESC")
    fun getActionsByType(typeLabel: String): Flow<List<ActionItem>>

    @Query("SELECT * FROM action_items WHERE response = :responseLabel ORDER BY createdAt DESC")
    fun getActionsByResponse(responseLabel: String): Flow<List<ActionItem>>

    @Query("""
        SELECT COUNT(*) 
        FROM action_items 
        WHERE response = :pendingLabel AND (deadline IS NULL OR deadline > :now)
    """)
    fun getPendingActionCount(
        pendingLabel: String = ActionResponse.PENDING.label,
        now: LocalDateTime
    ): Flow<Int>
}