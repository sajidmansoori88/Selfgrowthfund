package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.InvestmentDao
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.model.enums.InvestmentStatus
import com.selfgrowthfund.sgf.model.enums.InvesteeType
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.LocalDate
import javax.inject.Inject

class InvestmentRepository @Inject constructor(
    private val dao: InvestmentDao,
    private val dates: Dates
) {
    // CRUD
    suspend fun createInvestment(investment: Investment): Result<Unit> = try {
        dao.insert(investment)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateInvestment(investment: Investment): Result<Unit> = try {
        dao.update(investment)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteInvestment(id: String): Result<Unit> = try {
        dao.delete(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Get
    suspend fun getInvestment(id: String): Result<Investment> = try {
        Result.Success(dao.getById(id) ?: throw Exception("Investment not found"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getAllInvestments(): Flow<List<Investment>> = dao.getAll()

    // Use enum directly (not .name)
    fun getActiveInvestments(): Flow<List<Investment>> =
        dao.getByStatus(InvestmentStatus.Active)

    // Use enum directly (not .name)
    fun getInvestmentsByType(type: InvesteeType): Flow<List<Investment>> =
        dao.getByInvesteeType(type)

    // Business Logic
    suspend fun changeInvestmentStatus(id: String, newStatus: InvestmentStatus): Result<Unit> = try {
        val investment = dao.getById(id) ?: throw Exception("Investment not found")
        dao.update(investment.copy(status = newStatus))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getInvestmentsDueSoon(daysThreshold: Int = 7): Result<List<Investment>> = try {
        val today = LocalDate.now()
        val thresholdDate = today.plusDays(daysThreshold.toLong())
        Result.Success(dao.getDueBetween(today, thresholdDate))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun searchInvestments(query: String): Result<List<Investment>> = try {
        Result.Success(dao.search(query))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getTotalActiveInvestmentValue(): Result<Double> = try {
        Result.Success(dao.getTotalActiveAmount() ?: 0.0)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getActiveInvestmentCount(): Result<Int> = try {
        Result.Success(dao.getActiveCount())
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ID Preview
    suspend fun getLastInvestmentId(): String? = dao.getLastInvestmentId()

    // Summary DTO
    suspend fun getInvestmentSummary(): Result<InvestmentDao.InvestmentSummary> = try {
        Result.Success(dao.getInvestmentSummary())
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Overload methods that accept String parameters for compatibility
    suspend fun changeInvestmentStatus(id: String, newStatus: String): Result<Unit> = try {
        val investment = dao.getById(id) ?: throw Exception("Investment not found")
        val statusEnum = InvestmentStatus.valueOf(newStatus)
        dao.update(investment.copy(status = statusEnum))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Use safe conversion for string input
    fun getInvestmentsByType(type: String): Flow<List<Investment>> {
        val investeeType = try {
            InvesteeType.valueOf(type)
        } catch (e: IllegalArgumentException) {
            // Return empty flow or handle error appropriately
            return emptyFlow()
        }
        return dao.getByInvesteeType(investeeType)
    }
}