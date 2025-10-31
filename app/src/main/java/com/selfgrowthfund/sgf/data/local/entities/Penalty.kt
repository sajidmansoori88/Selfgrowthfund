package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.model.enums.PenaltyType
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "penalties")
@TypeConverters(AppTypeConverters::class)
data class Penalty(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val provisionalId: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val amount: Double,
    val type: PenaltyType,
    val reason: String,
    val recordedBy: String,
    val shareholderId: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val isSynced: Boolean = false
)

