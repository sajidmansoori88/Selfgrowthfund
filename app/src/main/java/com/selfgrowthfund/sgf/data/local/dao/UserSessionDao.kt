package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.selfgrowthfund.sgf.session.SessionEntry

@Dao
interface UserSessionDao {

    @Query("""
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
    """)
    suspend fun getSessionSummary(): List<SessionSummaryDb>
}

/**
 * Intermediate DB model – mapped to SessionEntry in repository
 */
data class SessionSummaryDb(
    val shareholderId: String,
    val lifetimeSessions: Int,
    val currentMonthSessions: Int
)
