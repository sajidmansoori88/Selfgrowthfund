package com.selfgrowthfund.sgf.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import java.util.Date
import java.time.YearMonth

class Converters {

    private val gson = Gson()

    // ✅ Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // ✅ List<String> converters with KSP-safe annotations
    @TypeConverter
    @JvmSuppressWildcards
    fun fromStringList(value: String?): List<String>? {
        return value?.let {
            gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
        }
    }

    @TypeConverter
    @JvmSuppressWildcards
    fun toStringList(list: List<String>?): String? {
        return list?.let {
            gson.toJson(it)
        }
    }
    @TypeConverter
    fun fromDueMonth(value: DueMonth?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toDueMonth(value: String?): DueMonth? {
        return value?.let { DueMonth.parse(it) }
    }


}