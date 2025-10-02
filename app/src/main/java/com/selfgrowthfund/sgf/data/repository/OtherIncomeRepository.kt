package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.OtherIncomeDao
import com.selfgrowthfund.sgf.data.local.entities.OtherIncome
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtherIncomeRepository @Inject constructor(
    private val dao: OtherIncomeDao
) {

    // --- CRUD ---
    suspend fun addIncome(otherIncome: OtherIncome) = dao.insertIncome(otherIncome)

    suspend fun getIncomesForUser(userId: String): List<OtherIncome> = dao.getIncomesByUser(userId)

    suspend fun getAllIncomes(): List<OtherIncome> = dao.getAllIncomes()

    suspend fun deleteIncome(otherIncome: OtherIncome) = dao.deleteIncome(otherIncome)

    suspend fun clearAll() = dao.clearAllIncomes()

    // --- Approval workflow ---
    suspend fun approve(
        incomeId: Long,
        approverId: String?,
        notes: String?,
        newStatus: ApprovalStage
    ): Boolean = withContext(Dispatchers.IO) {
        val rows = dao.updateApprovalStatus(
            incomeId = incomeId,
            status = newStatus,
            approvedBy = approverId,
            notes = notes,
            updatedAt = Instant.now()
        )
        rows > 0
    }

    suspend fun reject(
        incomeId: Long,
        rejectedBy: String?,
        notes: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val rows = dao.updateApprovalStatus(
            incomeId = incomeId,
            status = ApprovalStage.REJECTED,
            approvedBy = rejectedBy,
            notes = notes,
            updatedAt = Instant.now()
        )
        rows > 0
    }

    // --- Reports ---
    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        dao.countByStatus(ApprovalStage.APPROVED, start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        dao.countByStatus(ApprovalStage.REJECTED, start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        dao.countByStatus(ApprovalStage.PENDING, start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        dao.countTotal(start, end)

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<OtherIncome> =
        dao.getApprovalsBetween(start, end)

    suspend fun findById(incomeId: Long): OtherIncome? = dao.findById(incomeId)
}
