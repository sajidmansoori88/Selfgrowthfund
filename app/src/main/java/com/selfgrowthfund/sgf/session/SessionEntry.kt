package com.selfgrowthfund.sgf.session

data class SessionEntry(
    val sr: Int,
    val shareholderId: String,
    val name: String,
    val currentMonthSessions: Int,
    val lifetimeSessions: Int
)