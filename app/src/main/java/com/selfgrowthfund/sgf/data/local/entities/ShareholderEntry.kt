package com.selfgrowthfund.sgf.data.local.entities

import com.selfgrowthfund.sgf.model.enums.MemberRole
import java.util.Date

data class ShareholderEntry(
    val fullName: String,
    val mobileNumber: String,
    val email: String,
    val dob: Date,
    val address: String,
    val shareBalance: Double,
    val joiningDate: Date,
    val role: MemberRole
)