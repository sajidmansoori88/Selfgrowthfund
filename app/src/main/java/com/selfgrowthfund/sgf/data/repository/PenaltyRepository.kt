package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.PenaltyDao
import com.selfgrowthfund.sgf.data.local.entities.Penalty
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount

class PenaltyRepository(private val dao: PenaltyDao) {

    suspend fun addPenalty(penalty: Penalty) = dao.insert(penalty)

    suspend fun getTotalShareDepositPenalties(): Double = dao.getShareDepositPenalties()

    suspend fun getTotalBorrowingPenalties(): Double = dao.getBorrowingPenalties()

    suspend fun getTotalInvestmentPenalties(): Double = dao.getInvestmentPenalties()

    suspend fun getTotalOtherIncome(): Double = dao.getOtherIncome()

    suspend fun getMonthlyPenaltyTotal(month: String): Double = dao.getMonthlyPenaltyTotal(month)

    suspend fun getAllPenalties(): List<Penalty> = dao.getAllPenalties()

    suspend fun getPenaltiesByType(type: String): List<Penalty> = dao.getPenaltiesByType(type)

    suspend fun getPenaltiesByUser(userId: String): List<Penalty> = dao.getPenaltiesByUser(userId)

    suspend fun getPenaltiesByMonth(month: String): List<Penalty> = dao.getPenaltiesByMonth(month)
}