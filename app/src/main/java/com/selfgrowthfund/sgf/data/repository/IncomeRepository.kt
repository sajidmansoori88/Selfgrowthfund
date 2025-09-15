package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.OtherIncomeDao
import com.selfgrowthfund.sgf.data.local.entities.OtherIncomes
import java.time.LocalDate
import javax.inject.Inject

class IncomeRepository @Inject constructor(
    private val dao: OtherIncomeDao) {

    suspend fun addIncome(otherIncomes: OtherIncomes) = dao.insertIncome(otherIncomes)

    suspend fun getIncomesForUser(userId: String): List<OtherIncomes> = dao.getIncomesByUser(userId)

    suspend fun getAllIncomes(): List<OtherIncomes> = dao.getAllIncomes()

    suspend fun deleteIncome(otherIncomes: OtherIncomes) = dao.deleteIncome(otherIncomes)

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