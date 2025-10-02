package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.OtherIncome
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import java.time.Instant
import java.time.LocalDate

@Dao
interface OtherIncomeDao {

    // --- CRUD ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(otherIncome: OtherIncome)

    @Update
    suspend fun updateIncome(otherIncome: OtherIncome)

    @Delete
    suspend fun deleteIncome(otherIncome: OtherIncome)

    @Query("DELETE FROM other_incomes")
    suspend fun clearAllIncomes()

    // --- Queries ---
    @Query("SELECT * FROM other_incomes ORDER BY date DESC")
    suspend fun getAllIncomes(): List<OtherIncome>

    @Query("SELECT * FROM other_incomes WHERE recordedBy = :userId ORDER BY date DESC")
    suspend fun getIncomesByUser(userId: String): List<OtherIncome>

    @Query("SELECT * FROM other_incomes WHERE id = :incomeId LIMIT 1")
    suspend fun findById(incomeId: Long): OtherIncome?

    // --- Monthly reports ---
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

    // --- Approval workflow ---
    @Query("""
        UPDATE other_incomes
        SET approval_status = :status,
            approved_by = :approvedBy,
            approval_notes = :notes,
            updated_at = :updatedAt
        WHERE id = :incomeId
    """)
    suspend fun updateApprovalStatus(
        incomeId: Long,
        status: ApprovalStage,
        approvedBy: String?,
        notes: String?,
        updatedAt: Instant
    ): Int

    @Query("SELECT COUNT(*) FROM other_incomes WHERE approval_status = :status AND createdAt BETWEEN :start AND :end")
    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Int

    @Query("SELECT COUNT(*) FROM other_incomes WHERE createdAt BETWEEN :start AND :end")
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int

    @Query("SELECT * FROM other_incomes WHERE createdAt BETWEEN :start AND :end")
    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<OtherIncome>
}
