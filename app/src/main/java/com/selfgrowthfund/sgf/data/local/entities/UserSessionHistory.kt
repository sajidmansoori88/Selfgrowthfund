package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserSessionHistory")
data class UserSessionHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shareholderId: String,
    val timestamp: Long // store System.currentTimeMillis() when app is opened
)
