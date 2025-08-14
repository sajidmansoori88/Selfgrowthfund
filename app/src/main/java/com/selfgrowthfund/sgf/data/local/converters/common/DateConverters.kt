package com.selfgrowthfund.sgf.data.local.converters.common

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateConverters {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Optional: For string serialization
    @TypeConverter
    @JvmStatic
    fun fromDateString(value: String?): Date? {
        return try {
            value?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    @JvmStatic
    fun dateToString(date: Date?): String? {
        return date?.let { dateFormat.format(it) }
    }
}