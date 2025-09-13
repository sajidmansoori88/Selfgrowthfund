package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.ExpenseDao
import com.selfgrowthfund.sgf.data.local.entities.Expense
import java.time.LocalDate
import javax.inject.Inject


class ExpenseRepository @Inject constructor(
    private val dao: ExpenseDao) {

    suspend fun addExpense(expense: Expense) = dao.insertExpense(expense)

    suspend fun getExpensesForUser(userId: String): List<Expense> = dao.getExpensesByUser(userId)

    suspend fun getAllExpenses(): List<Expense> = dao.getAllExpenses()

    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

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