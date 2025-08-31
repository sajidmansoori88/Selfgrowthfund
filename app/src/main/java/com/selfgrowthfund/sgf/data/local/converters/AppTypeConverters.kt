package com.selfgrowthfund.sgf.data.local.converters

import androidx.room.TypeConverter
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.model.enums.*
import java.math.BigDecimal
import java.time.*
import java.util.*

object AppTypeConverters {

    // ───── Date/Time Converters ─────
    @TypeConverter @JvmStatic
    fun fromLocalDate(date: LocalDate?): Long? =
        date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    @TypeConverter @JvmStatic
    fun toLocalDate(timestamp: Long?): LocalDate? =
        timestamp?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }

    @TypeConverter @JvmStatic
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter @JvmStatic
    fun toInstant(timestamp: Long?): Instant? = timestamp?.let { Instant.ofEpochMilli(it) }

    @TypeConverter @JvmStatic
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? =
        dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    @TypeConverter @JvmStatic
    fun toLocalDateTime(millis: Long?): LocalDateTime? =
        millis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }

    @TypeConverter @JvmStatic
    fun fromYearMonth(value: YearMonth?): String? = value?.toString()

    @TypeConverter @JvmStatic
    fun toYearMonth(value: String?): YearMonth? = value?.let { YearMonth.parse(it) }

    // ───── Custom Types ─────
    @TypeConverter @JvmStatic
    fun fromDueMonth(dueMonth: DueMonth): String = dueMonth.value

    @TypeConverter @JvmStatic
    fun toDueMonth(value: String): DueMonth = DueMonth(value)

    @TypeConverter @JvmStatic
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter @JvmStatic
    fun toUUID(value: String?): UUID? = value?.let { UUID.fromString(it) }

    @TypeConverter @JvmStatic
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter @JvmStatic
    fun toBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }

    // ───── List & Map Converters ─────
    @TypeConverter @JvmStatic
    fun fromStringList(list: List<String>?): String? = list?.joinToString(",")

    @TypeConverter @JvmStatic
    fun toStringList(data: String?): List<String>? = data?.split(",")?.map { it.trim() }

    @TypeConverter @JvmStatic
    fun fromActionResponseMap(map: Map<String, ActionResponse>?): String? =
        map?.entries?.joinToString(";") { "${it.key}:${it.value.name}" }

    @TypeConverter @JvmStatic
    fun toActionResponseMap(data: String?): Map<String, ActionResponse> =
        data?.split(";")
            ?.mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) parts[0] to ActionResponse.valueOf(parts[1]) else null
            }?.toMap() ?: emptyMap()

    // ───── Enum Converters ─────
    @TypeConverter @JvmStatic fun fromBorrowingStatus(value: BorrowingStatus?): String? = value?.name
    @TypeConverter @JvmStatic fun toBorrowingStatus(name: String?): BorrowingStatus? = name?.let { BorrowingStatus.valueOf(it) }

    @TypeConverter @JvmStatic fun fromDepositStatus(value: DepositStatus?): String? = value?.name
    @TypeConverter @JvmStatic fun toDepositStatus(name: String?): DepositStatus? = name?.let { DepositStatus.valueOf(it) }

    @TypeConverter @JvmStatic fun fromPaymentMode(value: PaymentMode?): String? = value?.name
    @TypeConverter @JvmStatic fun toPaymentMode(name: String?): PaymentMode? = name?.let { PaymentMode.valueOf(it) }

    @TypeConverter @JvmStatic fun fromEntrySource(value: EntrySource?): String? = value?.name
    @TypeConverter @JvmStatic fun toEntrySource(name: String?): EntrySource? = name?.let { EntrySource.valueOf(it) }

    @TypeConverter @JvmStatic fun fromActionType(value: ActionType?): String? = value?.name
    @TypeConverter @JvmStatic fun toActionType(name: String?): ActionType? = name?.let { ActionType.valueOf(it) }

    @TypeConverter @JvmStatic fun fromActionResponse(value: ActionResponse?): String? = value?.name
    @TypeConverter @JvmStatic fun toActionResponse(name: String?): ActionResponse? = name?.let { ActionResponse.valueOf(it) }

    @TypeConverter @JvmStatic fun fromShareholderStatus(value: ShareholderStatus?): String? = value?.name
    @TypeConverter @JvmStatic fun toShareholderStatus(name: String?): ShareholderStatus? = name?.let { ShareholderStatus.valueOf(it) }

    @TypeConverter @JvmStatic fun fromPaymentStatus(value: PaymentStatus?): String? = value?.name
    @TypeConverter @JvmStatic fun toPaymentStatus(name: String?): PaymentStatus? = name?.let { PaymentStatus.valueOf(it) }

    @TypeConverter @JvmStatic fun fromTransactionType(value: TransactionType?): String? = value?.name
    @TypeConverter @JvmStatic fun toTransactionType(name: String?): TransactionType? = name?.let { TransactionType.valueOf(it) }

    @TypeConverter @JvmStatic fun fromPenaltyType(value: PenaltyType?): String? = value?.name
    @TypeConverter @JvmStatic fun toPenaltyType(name: String?): PenaltyType? = name?.let { PenaltyType.valueOf(it) }

    @TypeConverter @JvmStatic fun fromInvestmentStatus(value: InvestmentStatus?): String? = value?.name
    @TypeConverter @JvmStatic fun toInvestmentStatus(name: String?): InvestmentStatus? = name?.let { InvestmentStatus.valueOf(it) }

    @TypeConverter @JvmStatic
    fun fromMemberRoleList(list: List<MemberRole>?): String? = list?.joinToString(",") { it.name }

    @TypeConverter @JvmStatic
    fun toMemberRoleList(data: String?): List<MemberRole>? =
        data?.split(",")?.map { MemberRole.valueOf(it) }

    @TypeConverter @JvmStatic
    fun fromShareholderStatusList(list: List<ShareholderStatus>?): String? = list?.joinToString(",") { it.name }

    @TypeConverter @JvmStatic
    fun toShareholderStatusList(data: String?): List<ShareholderStatus>? =
        data?.split(",")?.map { ShareholderStatus.valueOf(it) }
}