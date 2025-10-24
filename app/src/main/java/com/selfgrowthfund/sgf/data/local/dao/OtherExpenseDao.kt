package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.OtherExpense
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import java.time.Instant
import java.time.LocalDate

@Dao
interface OtherExpenseDao {

    // --- CRUD ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(otherExpense: OtherExpense)

    @Update
    suspend fun updateExpense(otherExpense: OtherExpense)

    @Delete
    suspend fun deleteExpense(otherExpense: OtherExpense)

    @Query("DELETE FROM other_expenses")
    suspend fun clearAllExpenses()

    // --- Queries ---
    @Query("SELECT * FROM other_expenses ORDER BY date DESC")
    suspend fun getAllExpenses(): List<OtherExpense>

    @Query("SELECT * FROM other_expenses WHERE recordedBy = :userId ORDER BY date DESC")
    suspend fun getExpensesByUser(userId: String): List<OtherExpense>

    @Query("SELECT * FROM other_expenses WHERE id = :expenseId LIMIT 1")
    suspend fun findById(expenseId: Long): OtherExpense?

    // --- Aggregates ---
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

    // --- Approval workflow ---
    @Query("""
        UPDATE other_expenses
        SET approvalStatus = :status,
            approvedBy = :approvedBy,
            approvalNotes = :notes,
            updatedAt = :updatedAt
        WHERE id = :expenseId
    """)
    suspend fun updateApprovalStatus(
        expenseId: Long,
        status: ApprovalStage,
        approvedBy: String?,
        notes: String?,
        updatedAt: Instant
    ): Int

    @Query("SELECT COUNT(*) FROM other_expenses WHERE approvalStatus = :status AND createdAt BETWEEN :start AND :end")
    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Int

    @Query("SELECT COUNT(*) FROM other_expenses WHERE createdAt BETWEEN :start AND :end")
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int

    @Query("SELECT * FROM other_expenses WHERE createdAt BETWEEN :start AND :end")
    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<OtherExpense>

    // --- 🔁 SYNC HELPERS ---
    @Query("SELECT * FROM other_expenses WHERE isSynced = 0")
    suspend fun getUnsynced(): List<OtherExpense>

    @Query("SELECT * FROM other_expenses ORDER BY date DESC")
    suspend fun getAllExpensesList(): List<OtherExpense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<OtherExpense>)
}
