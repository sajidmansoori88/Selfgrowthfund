package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.selfgrowthfund.sgf.data.local.entities.UserSessionHistory

@Dao
interface UserSessionDao {

    // ✅ Aggregate summary per shareholder
    @Query(
        """
        SELECT 
            shareholderId,
            COUNT(*) as lifetimeSessions,
            SUM(
                CASE 
                    WHEN strftime('%Y-%m', timestamp/1000, 'unixepoch') = strftime('%Y-%m', 'now') 
                    THEN 1 ELSE 0 
                END
            ) as currentMonthSessions
        FROM UserSessionHistory
        GROUP BY shareholderId
        """
    )
    suspend fun getSessionSummary(): List<SessionSummaryDb>

    // ✅ Count only active members
    @Query("SELECT COUNT(*) FROM shareholders WHERE exitDate IS NULL AND shareholderStatus = 'Active'")
    suspend fun countActiveMembers(): Int

    // ✅ Full session list (use correct entity)
    @Query("SELECT * FROM UserSessionHistory")
    suspend fun getAllSessionsList(): List<UserSessionHistory>
}

/**
 * Projection model for aggregated sessions.
 * Not an entity — used only for query results.
 */
data class SessionSummaryDb(
    val shareholderId: String,
    val lifetimeSessions: Int,
    val currentMonthSessions: Int
)
