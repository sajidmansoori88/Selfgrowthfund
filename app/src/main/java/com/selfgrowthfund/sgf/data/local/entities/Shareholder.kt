package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.selfgrowthfund.sgf.data.local.types.ShareholderStatus
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.LocalDateTime

@Entity(tableName = "shareholders")
@TypeConverters(AppTypeConverters::class)
data class Shareholder(
    @PrimaryKey val shareholderId: String,

    val fullName: String,
    val mobileNumber: String,
    val address: String,

    val shareBalance: Double,
    val sharePrice: Double = 2000.0,

    val joinDate: LocalDateTime,
    val exitDate: LocalDateTime? = null,

    val status: ShareholderStatus = ShareholderStatus.Active,
    val lastUpdated: LocalDateTime = LocalDateTime.now(),

    var createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val MIN_SHARES = 1
        const val ELIGIBILITY_PERCENTAGE = 0.90

        fun generateNextId(lastId: String?): String {
            return lastId?.let {
                val num = it.removePrefix("SH").toInt()
                "SH%03d".format(num + 1)
            } ?: "SH001"
        }
    }

    fun calculateMaxBorrowAmount(): Double {
        return (shareBalance * sharePrice) * ELIGIBILITY_PERCENTAGE
    }

    fun isEligibleForLoan(): Boolean {
        return status == ShareholderStatus.Active && shareBalance >= MIN_SHARES
    }
}
