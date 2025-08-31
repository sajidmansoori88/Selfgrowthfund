package com.selfgrowthfund.sgf.data.local.entities


data class FundOverviewMetrics(
    val totalShareAmount: Double,
    val penaltiesFromShareDeposits: Double,
    val additionalContributions: Double,
    val penaltiesFromBorrowings: Double,
    val penaltiesFromInvestments: Double,
    val totalOtherIncomes: Double,
    val totalEarnings: Double,

    val totalInvestments: Double,
    val activeInvestments: Int,
    val closedInvestments: Int,
    val overdueInvestments: Int,
    val returnsFromClosedInvestments: Double,
    val writtenOffInvestments: Double,
    val investmentProfitPercent: Double,
    val investmentProfitAmount: Double,

    val totalBorrowIssued: Double,
    val activeBorrowings: Int,
    val closedBorrowings: Int,
    val repaymentsReceived: Double,
    val outstandingBorrowings: Double,
    val overdueBorrowings: Int,
    val otherExpenses: Double,
    val netProfitOrLoss: Double
)