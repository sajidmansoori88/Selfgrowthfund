package com.selfgrowthfund.sgf.features.addshareholders.ui.domain

import java.time.LocalDate

data class ShareholderInput(
    val name: String,
    val dateOfBirth: LocalDate,
    val mobileNumber: String,
    val email: String,
    val joiningDate: LocalDate,
    val role: String
)