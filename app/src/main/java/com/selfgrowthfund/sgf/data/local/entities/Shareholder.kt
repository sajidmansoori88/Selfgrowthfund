package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.enums.ShareholderStatus
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "shareholders")
@TypeConverters(AppTypeConverters::class)
data class Shareholder(
    @PrimaryKey
    val shareholderId: String,      // Format: SH001, SH002 etc.

    // Core Information
    val fullName: String,
    val dob: LocalDate?,
    val email: String,
    val mobileNumber: String,
    val address: String,

    // Share Details
    val shareBalance: Double,
    val sharePrice: Double = 2000.0,

    // Membership Dates
    val joiningDate: LocalDate?,
    val exitDate: LocalDate? = null,

    // Member Role (now enum-based ✅)
    val role: MemberRole = MemberRole.MEMBER,

    // Status (already enum-based ✅)
    val shareholderStatus: ShareholderStatus = ShareholderStatus.Active,

    val lastUpdated: Instant = Instant.now(),

    val roleHistory: List<MemberRole> = emptyList(),
    val statusHistory: List<ShareholderStatus> = emptyList(),


    // Audit Fields
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
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
        return shareholderStatus == ShareholderStatus.Active && shareBalance >= MIN_SHARES
    }

    fun isValid(): Boolean {
        return fullName.isNotBlank() &&
                mobileNumber.length == 10 &&
                shareBalance >= MIN_SHARES
    }

    fun isActive(): Boolean =
        exitDate == null && shareholderStatus == ShareholderStatus.Active
}