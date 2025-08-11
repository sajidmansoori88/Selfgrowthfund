package com.selfgrowthfund.sgf.data.deposit

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.selfgrowthfund.sgf.data.local.entities.Deposit

class FakeDepositRepository : DepositRepositoryInterface {

    override suspend fun addDeposit(deposit: Deposit) {
        println("Fake insert: $deposit")
    }

    override fun getDeposits(): Flow<List<Deposit>> {
        val sampleDeposit = Deposit(
            depositId = "D001", // Changed from Int to String
            shareholderId = "1",
            shareholderName = "John Doe", // Added
            dueMonth = "Aug-2025",
            paymentDate = "11/08/2025",
            shareNos = 2,
            additionalContribution = 500.0,
            penalty = 20.0,
            totalAmount = 4520.0,
            paymentStatus = "Late",
            modeOfPayment = "Cash" // Added
        )
        return flowOf(listOf(sampleDeposit))
    }
}