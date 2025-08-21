package com.selfgrowthfund.sgf.data.local.converters

import androidx.room.TypeConverter
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.model.enums.EntrySource
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.Date

object AppTypeConverters {

    // ===== org.threeten.bp.LocalDate ↔ Long =====
    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDate(timestamp: Long?): LocalDate? {
        return timestamp?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    // ===== org.threeten.bp.Instant ↔ Long =====
    @TypeConverter
    @JvmStatic
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    @JvmStatic
    fun toInstant(timestamp: Long?): Instant? {
        return timestamp?.let { Instant.ofEpochMilli(it) }
    }

    // ===== java.util.Date ↔ Long =====
    @TypeConverter
    @JvmStatic
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?): Date? = timestamp?.let { Date(it) }

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

    // ===== DueMonth ↔ String =====
    @TypeConverter
    @JvmStatic
    fun fromDueMonth(dueMonth: DueMonth): String = dueMonth.value

    @TypeConverter
    @JvmStatic
    fun toDueMonth(value: String): DueMonth = DueMonth(value)

    // ===== EntrySource ↔ String =====
    @TypeConverter
    @JvmStatic
    fun fromEntrySource(source: EntrySource?): String? = source?.name

    @TypeConverter
    @JvmStatic
    fun toEntrySource(value: String?): EntrySource? =
        value?.let { EntrySource.valueOf(it) }
}