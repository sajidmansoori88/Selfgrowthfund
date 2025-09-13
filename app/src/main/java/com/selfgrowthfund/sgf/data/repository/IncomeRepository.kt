package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.IncomeDao
import com.selfgrowthfund.sgf.data.local.entities.Income
import java.time.LocalDate
import javax.inject.Inject

class IncomeRepository @Inject constructor(
    private val dao: IncomeDao) {

    suspend fun addIncome(income: Income) = dao.insertIncome(income)

    suspend fun getIncomesForUser(userId: String): List<Income> = dao.getIncomesByUser(userId)

    suspend fun getAllIncomes(): List<Income> = dao.getAllIncomes()

    suspend fun deleteIncome(income: Income) = dao.deleteIncome(income)

    suspend fun clearAll() = dao.clearAllIncomes()
    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        dao.countByStatus("APPROVED", start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        dao.countByStatus("REJECTED", start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        dao.countByStatus("PENDING", start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        dao.countTotal(start, end)
}