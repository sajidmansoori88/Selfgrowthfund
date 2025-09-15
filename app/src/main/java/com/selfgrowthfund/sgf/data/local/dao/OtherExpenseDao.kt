package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.OtherExpenses
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import java.time.LocalDate

@Dao
interface OtherExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(otherExpenses: OtherExpenses)

    @Query("SELECT * FROM other_expenses ORDER BY date DESC")
    suspend fun getAllExpenses(): List<OtherExpenses>

    @Query("SELECT * FROM other_expenses WHERE recordedBy = :userId ORDER BY date DESC")
    suspend fun getExpensesByUser(userId: String): List<OtherExpenses>

    @Delete
    suspend fun deleteExpense(otherExpenses: OtherExpenses)

    @Query("DELETE FROM other_expenses")
    suspend fun clearAllExpenses()
    @Query("SELECT SUM(amount) FROM other_expenses")
    suspend fun getTotalExpenses(): Double

    @Query("""
    SELECT strftime('%Y-%m', date) AS month, SUM(amount) AS total
    FROM other_expenses
    GROUP BY month
    ORDER BY month ASC
""")
    suspend fun getMonthlyExpenses(): List<MonthlyAmount>

    @Query("""
    SELECT SUM(amount)
    FROM other_expenses
    WHERE strftime('%Y-%m', date) = :month
""")
    suspend fun getMonthlyExpenses(month: String): Double

    @Query("SELECT COUNT(*) FROM investments WHERE approvalStatus = :status AND createdAt BETWEEN :start AND :end")
    suspend fun countByStatus(status: String, start: LocalDate, end: LocalDate): Int

    @Query("SELECT COUNT(*) FROM investments WHERE createdAt BETWEEN :start AND :end")
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int
}