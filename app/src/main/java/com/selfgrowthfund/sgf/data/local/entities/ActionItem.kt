package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.model.enums.ActionType
import com.selfgrowthfund.sgf.model.enums.ActionResponse
import java.time.LocalDateTime

@Entity(tableName = "action_items")
@TypeConverters(AppTypeConverters::class)
data class ActionItem(
    @PrimaryKey val actionId: String,
    val type: ActionType,
    val relatedEntityId: String,
    val title: String,
    val description: String,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val deadline: LocalDateTime?,
    val response: ActionResponse,

    // Add explicit column info for the map
    @ColumnInfo(name = "responses")
    val responses: Map<String, ActionResponse> = emptyMap(),

    val finalized: Boolean = false
)