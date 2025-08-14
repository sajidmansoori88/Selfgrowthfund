package com.selfgrowthfund.sgf.ui.extensions

import com.selfgrowthfund.sgf.R
import com.selfgrowthfund.sgf.model.enums.DepositStatus

fun DepositStatus.iconRes(): Int = when (this) {
    DepositStatus.Pending -> R.drawable.ic_pending
    DepositStatus.Approved -> R.drawable.ic_approved
    DepositStatus.Rejected -> R.drawable.ic_rejected
    DepositStatus.AutoRejected -> R.drawable.ic_auto_rejected
}