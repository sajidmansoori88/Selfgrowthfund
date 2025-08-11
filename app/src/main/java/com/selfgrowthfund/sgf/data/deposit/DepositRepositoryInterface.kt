package com.selfgrowthfund.sgf.data.deposit

import com.selfgrowthfund.sgf.data.local.entities.Deposit
import kotlinx.coroutines.flow.Flow

interface DepositRepositoryInterface {
    suspend fun addDeposit(deposit: Deposit)
    fun getDeposits(): Flow<List<Deposit>>
}