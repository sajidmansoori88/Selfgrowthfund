package com.selfgrowthfund.sgf.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {
    // Existing Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    // New List<String> converter
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.let {
            Gson().fromJson(it, object : TypeToken<List<String>>() {}.type)
        }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.let {
            Gson().toJson(it)
        }
    }
}