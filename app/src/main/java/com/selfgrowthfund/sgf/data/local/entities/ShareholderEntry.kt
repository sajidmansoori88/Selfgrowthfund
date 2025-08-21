package com.selfgrowthfund.sgf.data.local.entities

import org.threeten.bp.LocalDate

data class ShareholderEntry(
    val fullName: String,
    val mobileNumber: String,
    val email: String,
    val dob: LocalDate?,           // Nullable for optional DOB
    val address: String,
    val shareBalance: Double,
    val joiningDate: LocalDate?,   // Nullable for draft entries
    val role: String               // Stored as enum name string
)