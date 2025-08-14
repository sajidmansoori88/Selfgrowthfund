package com.selfgrowthfund.sgf.data.local.converters.enums

import androidx.room.TypeConverter
import com.selfgrowthfund.sgf.data.local.types.*

/**
 * Handles conversion of all enums to/from their string representations.
 * Includes fallback values for corrupted database entries.
 */
object StatusConverters {

    // DepositStatus
    @TypeConverter @JvmStatic
    fun fromDepositStatus(status: DepositStatus?): String? = status?.name

    @TypeConverter @JvmStatic
    fun toDepositStatus(value: String?): DepositStatus? =
        value?.let { runCatching { DepositStatus.valueOf(it) }.getOrNull() }

    // BorrowingStatus
    @TypeConverter @JvmStatic
    fun fromBorrowingStatus(status: BorrowingStatus?): String? = status?.name

    @TypeConverter @JvmStatic
    fun toBorrowingStatus(value: String?): BorrowingStatus? =
        value?.let { BorrowingStatus.entries.firstOrNull { e -> e.name == it } }

    // PaymentStatus
    @TypeConverter @JvmStatic
    fun fromPaymentStatus(status: PaymentStatus?): String? = status?.name

    @TypeConverter @JvmStatic
    fun toPaymentStatus(value: String?): PaymentStatus? =
        value?.let { PaymentStatus.entries.firstOrNull { e -> e.name == it } }

    // ShareholderStatus
    @TypeConverter @JvmStatic
    fun fromShareholderStatus(status: ShareholderStatus?): String? = status?.name

    @TypeConverter @JvmStatic
    fun toShareholderStatus(value: String?): ShareholderStatus? =
        value?.let { runCatching { ShareholderStatus.valueOf(it) }.getOrNull() }

    // PaymentMode (with fallback)
    @TypeConverter @JvmStatic
    fun fromPaymentMode(mode: PaymentMode?): String? = mode?.name

    @TypeConverter @JvmStatic
    fun toPaymentMode(value: String?): PaymentMode? =
        value?.let { runCatching { PaymentMode.valueOf(it) }.getOrNull() }
}