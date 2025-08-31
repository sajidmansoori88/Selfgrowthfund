package com.selfgrowthfund.sgf.model.reports

import androidx.room.Embedded
import com.selfgrowthfund.sgf.data.local.entities.Shareholder

data class ShareholderWithEligibility(
    @Embedded val shareholder: Shareholder,
    val maxBorrowAmount: Double
)

data class ShareholderLoanStatus(
    @Embedded val shareholder: Shareholder,
    val maxBorrowAmount: Double,
    val currentLoans: Double
)