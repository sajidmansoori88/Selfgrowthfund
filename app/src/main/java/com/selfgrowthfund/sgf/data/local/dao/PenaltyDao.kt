package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.selfgrowthfund.sgf.data.local.entities.Penalty
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import com.selfgrowthfund.sgf.model.reports.ShareholderPenaltySummary
import java.time.LocalDate

@Dao
interface PenaltyDao {

    @Insert
    suspend fun insert(penalty: Penalty)

    @Query("SELECT SUM(amount) FROM penalties WHERE type = 'SHARE_DEPOSIT'")
    suspend fun getShareDepositPenalties(): Double

    @Query("SELECT SUM(amount) FROM penalties WHERE type = 'BORROWING'")
    suspend fun getBorrowingPenalties(): Double

    @Query("SELECT SUM(amount) FROM penalties WHERE type = 'INVESTMENT'")
    suspend fun getInvestmentPenalties(): Double

    @Query("SELECT SUM(amount) FROM penalties WHERE type = 'OTHER'")
    suspend fun getOtherIncome(): Double

    @Query("SELECT SUM(amount) FROM penalties WHERE strftime('%Y-%m', date) = :month")
    suspend fun getMonthlyPenaltyTotal(month: String): Double

    @Query("SELECT * FROM penalties ORDER BY date DESC")
    suspend fun getAllPenalties(): List<Penalty>

    @Query("SELECT * FROM penalties WHERE type = :type ORDER BY date DESC")
    suspend fun getPenaltiesByType(type: String): List<Penalty>

    @Query("SELECT * FROM penalties WHERE recordedBy = :shareholderId ORDER BY date DESC")
    suspend fun getPenaltiesByUser(shareholderId: String): List<Penalty>

    @Query("SELECT * FROM penalties WHERE strftime('%Y-%m', date) = :month ORDER BY date DESC")
    suspend fun getPenaltiesByMonth(month: String): List<Penalty>

    @Query("""
    SELECT strftime('%Y-%m', date) AS month, SUM(amount) AS total
    FROM penalties
    GROUP BY month
    ORDER BY month ASC
""")
    suspend fun getMonthlyPenalties(): List<MonthlyAmount>
    @Query("""
        SELECT shareholderId, SUM(amount) AS totalPenalties
        FROM penalties
        GROUP BY shareholderId
    """)
    suspend fun getShareholderPenaltySummary(): List<ShareholderPenaltySummary>
    @Query("SELECT * FROM penalties WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getPenaltiesBetween(startDate: LocalDate, endDate: LocalDate): List<Penalty>

}