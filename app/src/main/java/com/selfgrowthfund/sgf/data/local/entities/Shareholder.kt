package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.model.enums.MemberRole
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

@Entity(tableName = "shareholders")
data class Shareholder(
    @PrimaryKey
    val shareholderId: String,      // Format: SH001, SH002 etc.

    // Core Information
    val fullName: String,
    val dob: LocalDate,
    val email: String,
    val mobileNumber: String,
    val address: String,

    // Share Details
    val shareBalance: Double,
    val sharePrice: Double = 2000.0,

    // Membership Dates
    val joiningDate: LocalDate,
    val exitDate: LocalDate? = null,

    // Member Roles
    val role: MemberRole,

    // Status
    val shareholderStatus: String = STATUS_ACTIVE,
    val lastUpdated: Instant = Instant.now(),

    // Audit Fields
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
) {
    companion object {
        const val STATUS_ACTIVE = "Active"
        const val STATUS_INACTIVE = "Inactive"
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
        return shareholderStatus == STATUS_ACTIVE && shareBalance >= MIN_SHARES
    }

    fun isValid(): Boolean {
        return fullName.isNotBlank() &&
                mobileNumber.length == 10 &&
                shareBalance >= MIN_SHARES
    }

    fun isActive(): Boolean = exitDate == null && shareholderStatus == STATUS_ACTIVE
}