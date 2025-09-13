package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Income
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import java.time.LocalDate


@Dao
interface IncomeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: Income)

    @Query("SELECT * FROM incomes ORDER BY date DESC")
    suspend fun getAllIncomes(): List<Income>

    @Query("SELECT * FROM incomes WHERE recordedBy = :userId ORDER BY date DESC")
    suspend fun getIncomesByUser(userId: String): List<Income>

    @Delete
    suspend fun deleteIncome(income: Income)

    @Query("DELETE FROM incomes")
    suspend fun clearAllIncomes()
    @Query("""
    SELECT strftime('%Y-%m', date) AS month, SUM(amount) AS total
    FROM incomes
    GROUP BY month
    ORDER BY month ASC
""")
    suspend fun getMonthlyOtherIncome(): List<MonthlyAmount>

    @Query("""
    SELECT SUM(amount)
    FROM incomes
    WHERE strftime('%Y-%m', date) = :month
""")
    suspend fun getMonthlyIncome(month: String): Double
    @Query("SELECT COUNT(*) FROM investments WHERE approvalStatus = :status AND createdAt BETWEEN :start AND :end")
    suspend fun countByStatus(status: String, start: LocalDate, end: LocalDate): Int

    @Query("SELECT COUNT(*) FROM investments WHERE createdAt BETWEEN :start AND :end")
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int
}