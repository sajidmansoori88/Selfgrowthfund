package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.OtherExpenseDao
import com.selfgrowthfund.sgf.data.local.entities.OtherExpenses
import java.time.LocalDate
import javax.inject.Inject


class ExpenseRepository @Inject constructor(
    private val dao: OtherExpenseDao) {

    suspend fun addExpense(otherExpenses: OtherExpenses) = dao.insertExpense(otherExpenses)

    suspend fun getExpensesForUser(userId: String): List<OtherExpenses> = dao.getExpensesByUser(userId)

    suspend fun getAllExpenses(): List<OtherExpenses> = dao.getAllExpenses()

    suspend fun deleteExpense(otherExpenses: OtherExpenses) = dao.deleteExpense(otherExpenses)

    suspend fun clearAll() = dao.clearAllExpenses()
    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        dao.countByStatus("APPROVED", start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        dao.countByStatus("REJECTED", start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        dao.countByStatus("PENDING", start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        dao.countTotal(start, end)
}