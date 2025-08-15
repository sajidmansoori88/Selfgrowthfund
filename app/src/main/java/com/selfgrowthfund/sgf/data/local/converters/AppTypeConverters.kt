package com.selfgrowthfund.sgf.data.local.converters

import android.os.Build
import androidx.room.TypeConverter
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger

object AppTypeConverters {

    private val logger = Logger.getLogger("AppTypeConverters")
    private val fallbackFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ===== LocalDateTime ↔ Long =====
    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dateTime?.toEpochSecond(ZoneOffset.UTC)
        } else {
            logger.warning("LocalDateTime not supported on API < 26")
            null
        }
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(timestamp: Long?): LocalDateTime? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timestamp?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
        } else {
            logger.warning("LocalDateTime not supported on API < 26")
            null
        }
    }

    // ===== LocalDate ↔ String =====
    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            logger.warning("LocalDate not supported on API < 26")
            null
        }
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDate(value: String?): LocalDate? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            value?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        } else {
            logger.warning("LocalDate not supported on API < 26")
            null
        }
    }

    // ===== List<String> ↔ String =====
    @TypeConverter
    @JvmStatic
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(data: String?): List<String>? {
        return data?.split(",")?.map { it.trim() }
    }

    // ===== Date ↔ Long =====
    @TypeConverter
    @JvmStatic
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?): Date? = timestamp?.let { Date(it) }

    // ===== DueMonth ↔ String =====
    @TypeConverter
    @JvmStatic
    fun fromDueMonth(dueMonth: DueMonth): String = dueMonth.value

    @TypeConverter
    @JvmStatic
    fun toDueMonth(value: String): DueMonth = DueMonth(value)
}