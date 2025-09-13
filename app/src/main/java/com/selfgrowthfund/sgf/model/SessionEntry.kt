package com.selfgrowthfund.sgf.model

data class SessionEntry(
    val shareholderId: String,
    val name: String,
    val currentMonthSessions: Int,
    val lifetimeSessions: Int
)