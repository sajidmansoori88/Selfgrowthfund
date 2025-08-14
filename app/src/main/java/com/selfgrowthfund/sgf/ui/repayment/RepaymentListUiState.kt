package com.selfgrowthfund.sgf.ui.repayment

import com.selfgrowthfund.sgf.data.local.entities.Repayment

data class RepaymentListUiState(
    val isLoading: Boolean = false,
    val repayments: List<Repayment> = emptyList(),
    val error: Throwable? = null
)