package com.selfgrowthfund.sgf.data.local.converters

import androidx.room.TypeConverter
import com.selfgrowthfund.sgf.model.enums.*

class EnumConverters {

    @TypeConverter
    fun fromDepositStatus(value: DepositStatus): String = value.name

    @TypeConverter
    fun toDepositStatus(value: String): DepositStatus = DepositStatus.valueOf(value)

    @TypeConverter
    fun fromPaymentMode(value: PaymentMode): String = value.name

    @TypeConverter
    fun toPaymentMode(value: String): PaymentMode = PaymentMode.valueOf(value)

    @TypeConverter
    fun fromBorrowingStatus(value: BorrowingStatus): String = value.name

    @TypeConverter
    fun toBorrowingStatus(value: String): BorrowingStatus = BorrowingStatus.valueOf(value)

    @TypeConverter
    fun fromShareholderStatus(value: ShareholderStatus): String = value.name

    @TypeConverter
    fun toShareholderStatus(value: String): ShareholderStatus = ShareholderStatus.valueOf(value)

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = PaymentStatus.valueOf(value)

    @TypeConverter
    fun fromInvesteeType(value: InvesteeType): String = value.name

    @TypeConverter
    fun toInvesteeType(value: String): InvesteeType = InvesteeType.valueOf(value)

    @TypeConverter
    fun fromOwnershipType(value: OwnershipType): String = value.name

    @TypeConverter
    fun toOwnershipType(value: String): OwnershipType = OwnershipType.valueOf(value)

    @TypeConverter
    fun fromInvestmentStatus(value: InvestmentStatus): String = value.name

    @TypeConverter
    fun toInvestmentStatus(value: String): InvestmentStatus = InvestmentStatus.valueOf(value)

    @TypeConverter
    fun fromRole(role: MemberRole): String = role.name

    @TypeConverter
    fun toRole(name: String): MemberRole = MemberRole.valueOf(name)

    @TypeConverter
    fun fromEntrySource(value: EntrySource): String = value.name

    @TypeConverter
    fun toEntrySource(value: String): EntrySource = EntrySource.valueOf(value)

}