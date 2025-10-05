package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface UserSessionDao {

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

    @Query("SELECT COUNT(*) FROM shareholders WHERE exitDate IS NULL AND shareholderStatus = 'Active'")
    suspend fun countActiveMembers(): Int
}

/**
 * Intermediate DB model – mapped to SessionEntry in repository
 */
data class SessionSummaryDb(
    val shareholderId: String,
    val lifetimeSessions: Int,
    val currentMonthSessions: Int
)
