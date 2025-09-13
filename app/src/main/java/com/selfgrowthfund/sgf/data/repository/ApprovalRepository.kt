package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.ui.admin.ApprovalSummaryRow
import java.time.LocalDate
import javax.inject.Inject

class ApprovalRepository @Inject constructor(
    private val depositRepository: DepositRepository,
    private val borrowingRepository: BorrowingRepository,
    private val repaymentRepository: RepaymentRepository,
    private val investmentRepository: InvestmentRepository,
    private val investmentReturnsRepository: InvestmentReturnsRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
) {
    suspend fun getApprovalSummary(start: LocalDate, end: LocalDate): List<ApprovalSummaryRow> {
        val results = mutableListOf<ApprovalSummaryRow>()

        results.add(
            ApprovalSummaryRow(
                type = "Deposits",
                approved = depositRepository.countApproved(start, end),
                rejected = depositRepository.countRejected(start, end),
                pending = depositRepository.countPending(start, end)
            )
        )

        results.add(
            ApprovalSummaryRow(
                type = "Borrowing",
                approved = borrowingRepository.countApproved(start, end),
                rejected = borrowingRepository.countRejected(start, end),
                pending = borrowingRepository.countPending(start, end)
            )
        )

        results.add(
            ApprovalSummaryRow(
                type = "Repayments",
                approved = repaymentRepository.countApproved(start, end),
                rejected = repaymentRepository.countRejected(start, end),
                pending = repaymentRepository.countPending(start, end)
            )
        )

        results.add(
            ApprovalSummaryRow(
                type = "Investments",
                approved = investmentRepository.countApproved(start, end),
                rejected = investmentRepository.countRejected(start, end),
                pending = investmentRepository.countPending(start, end)
            )
        )

        results.add(
            ApprovalSummaryRow(
                type = "Investment Returns",
                approved = investmentReturnsRepository.countApproved(start, end),
                rejected = investmentReturnsRepository.countRejected(start, end),
                pending = investmentReturnsRepository.countPending(start, end)
            )
        )

        results.add(
            ApprovalSummaryRow(
                type = "Income",
                approved = incomeRepository.countApproved(start, end),
                rejected = incomeRepository.countRejected(start, end),
                pending = incomeRepository.countPending(start, end)
            )
        )

        results.add(
            ApprovalSummaryRow(
                type = "Expenses",
                approved = expenseRepository.countApproved(start, end),
                rejected = expenseRepository.countRejected(start, end),
                pending = expenseRepository.countPending(start, end)
            )
        )

        return results
    }
}
