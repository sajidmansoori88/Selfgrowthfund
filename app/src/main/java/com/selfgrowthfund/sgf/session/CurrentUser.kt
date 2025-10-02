package com.selfgrowthfund.sgf.session

import com.selfgrowthfund.sgf.model.enums.MemberRole

data class CurrentUser(
    val userId: String,   // Firebase UID or shareholderId
    val email: String,
    val role: MemberRole
)
