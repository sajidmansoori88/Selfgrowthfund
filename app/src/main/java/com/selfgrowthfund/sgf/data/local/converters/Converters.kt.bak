package com.selfgrowthfund.sgf.data.local.converters

import androidx.room.TypeConverter
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import java.util.Date

class Converters {

    // ✅ Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromDueMonth(dueMonth: DueMonth): String = dueMonth.value

    @TypeConverter
    fun toDueMonth(value: String): DueMonth = DueMonth(value)


    // 🧩 Add more converters below as needed
    // For example: LocalDateTime, enums, lists, etc.
}