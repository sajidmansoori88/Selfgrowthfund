package com.selfgrowthfund.sgf.data.local.converters.custom

import androidx.room.TypeConverter
import com.selfgrowthfund.sgf.data.local.types.DueMonth

object DueMonthConverter {
    /**
     * Converts to database string format
     * @return String in "MMM-yyyy" format or null
     */
    @TypeConverter
    @JvmStatic
    fun fromDueMonth(dueMonth: DueMonth?): String? = dueMonth?.toString()

    /**
     * Parses from database with fallback to default
     * @return DueMonth or null (if input is null)
     */
    @TypeConverter
    @JvmStatic
    fun toDueMonth(value: String?): DueMonth? =
        value?.let { DueMonth.parse(it, fallbackToDefault = true) }
}