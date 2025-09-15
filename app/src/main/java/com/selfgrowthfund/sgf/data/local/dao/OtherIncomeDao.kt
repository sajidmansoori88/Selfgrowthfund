package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.OtherIncomes
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import java.time.LocalDate


@Dao
interface OtherIncomeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(otherIncomes: OtherIncomes)

    @Query("SELECT * FROM other_incomes ORDER BY date DESC")
    suspend fun getAllIncomes(): List<OtherIncomes>

    @Query("SELECT * FROM other_incomes WHERE recordedBy = :userId ORDER BY date DESC")
    suspend fun getIncomesByUser(userId: String): List<OtherIncomes>

    @Delete
    suspend fun deleteIncome(otherIncomes: OtherIncomes)

    @Query("DELETE FROM other_incomes")
    suspend fun clearAllIncomes()
    @Query("""
    SELECT strftime('%Y-%m', date) AS month, SUM(amount) AS total
    FROM other_incomes
    GROUP BY month
    ORDER BY month ASC
""")
    suspend fun getMonthlyOtherIncome(): List<MonthlyAmount>

    @Query("""
    SELECT SUM(amount)
    FROM other_incomes
    WHERE strftime('%Y-%m', date) = :month
""")
    suspend fun getMonthlyIncome(month: String): Double
    @Query("SELECT COUNT(*) FROM investments WHERE approvalStatus = :status AND createdAt BETWEEN :start AND :end")
    suspend fun countByStatus(status: String, start: LocalDate, end: LocalDate): Int

    @Query("SELECT COUNT(*) FROM investments WHERE createdAt BETWEEN :start AND :end")
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int
}