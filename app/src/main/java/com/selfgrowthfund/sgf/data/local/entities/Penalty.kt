package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.model.enums.PenaltyType
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "penalties")
@TypeConverters(AppTypeConverters::class)
data class Penalty(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amount: Double,
    val type: PenaltyType,
    val reason: String,
    val recordedBy: String, // adminId or system user
    val shareholderId: String, // NEW: links penalty to a shareholder
    val createdAt: Instant = Instant.now()
)