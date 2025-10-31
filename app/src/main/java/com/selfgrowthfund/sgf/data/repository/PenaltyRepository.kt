package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.PenaltyDao
import com.selfgrowthfund.sgf.data.local.entities.Penalty
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import com.selfgrowthfund.sgf.utils.Result
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PenaltyRepository (Realtime Firestore <-> Room Sync)
 *
 * - Local writes mark isSynced = false.
 * - realtimeSyncRepository.pushAllUnsynced() triggers Firestore sync.
 * - Firestore -> Room handled automatically by RealtimeSyncRepository listener.
 */
@Singleton
class PenaltyRepository @Inject constructor(
    private val dao: PenaltyDao,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    // =============================
    // CRUD Operations
    // =============================

    suspend fun addPenalty(penalty: Penalty): Result<Unit> = try {
        val now = Instant.now()
        dao.insert(penalty.copy(isSynced = false, updatedAt = now))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updatePenalty(penalty: Penalty): Result<Unit> = try {
        val now = Instant.now()
        dao.insertAll(listOf(penalty.copy(isSynced = false, updatedAt = now)))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deletePenalty(penalty: Penalty): Result<Unit> = try {
        dao.deletePenalty(penalty)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // Queries
    // =============================

    suspend fun getTotalShareDepositPenalties(): Double = dao.getShareDepositPenalties()
    suspend fun getTotalBorrowingPenalties(): Double = dao.getBorrowingPenalties()
    suspend fun getTotalInvestmentPenalties(): Double = dao.getInvestmentPenalties()
    suspend fun getTotalOtherIncome(): Double = dao.getOtherIncome()

    suspend fun getMonthlyPenaltyTotal(month: String): Double = dao.getMonthlyPenaltyTotal(month)

    suspend fun getAllPenalties(): List<Penalty> = dao.getAllPenalties()

    suspend fun getPenaltiesByType(type: String): List<Penalty> = dao.getPenaltiesByType(type)

    suspend fun getPenaltiesByUser(userId: String): List<Penalty> = dao.getPenaltiesByUser(userId)

    suspend fun getPenaltiesByMonth(month: String): List<Penalty> = dao.getPenaltiesByMonth(month)

    suspend fun getPenaltiesBetween(startDate: LocalDate, endDate: LocalDate): List<Penalty> =
        dao.getPenaltiesBetween(startDate, endDate)
}
