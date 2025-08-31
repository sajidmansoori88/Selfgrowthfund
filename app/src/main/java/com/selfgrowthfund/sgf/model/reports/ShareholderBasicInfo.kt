package com.selfgrowthfund.sgf.model.reports

import java.time.LocalDate

data class ShareholderBasicInfo(
    val shareholderId: String,
    val fullName: String,
    val shareBalance: Double,
    val joiningDate: LocalDate
)