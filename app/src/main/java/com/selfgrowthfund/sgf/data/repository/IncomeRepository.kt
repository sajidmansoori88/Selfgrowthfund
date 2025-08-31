package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.IncomeDao
import com.selfgrowthfund.sgf.data.local.entities.Income

class IncomeRepository(private val dao: IncomeDao) {

    suspend fun addIncome(income: Income) = dao.insertIncome(income)

    suspend fun getIncomesForUser(userId: String): List<Income> = dao.getIncomesByUser(userId)

    suspend fun getAllIncomes(): List<Income> = dao.getAllIncomes()

    suspend fun deleteIncome(income: Income) = dao.deleteIncome(income)

    suspend fun clearAll() = dao.clearAllIncomes()
}