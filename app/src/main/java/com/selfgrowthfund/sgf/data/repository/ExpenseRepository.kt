package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.ExpenseDao
import com.selfgrowthfund.sgf.data.local.entities.Expense


class ExpenseRepository(private val dao: ExpenseDao) {

    suspend fun addExpense(expense: Expense) = dao.insertExpense(expense)

    suspend fun getExpensesForUser(userId: String): List<Expense> = dao.getExpensesByUser(userId)

    suspend fun getAllExpenses(): List<Expense> = dao.getAllExpenses()

    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    suspend fun clearAll() = dao.clearAllExpenses()
}