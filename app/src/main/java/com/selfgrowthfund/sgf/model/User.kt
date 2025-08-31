package com.selfgrowthfund.sgf.model

import com.selfgrowthfund.sgf.model.enums.MemberRole

data class User(
    val id: String,
    val name: String,
    val role: MemberRole,
    val shareholderId: String
)