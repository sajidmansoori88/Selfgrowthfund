package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.utils.Dates
import java.util.*

@Entity(tableName = "shareholders")
data class Shareholder(
    @PrimaryKey
    val shareholderId: String,      // Format: SH001, SH002 etc.

    // Core Information
    val fullName: String,           // Member's full name
    val dob: Date,                  // Member's date of birth
    val email: String,              // Member's email
    val mobileNumber: String,       // Required contact number
    val address: String,            // Current address

    // Share Details
    val shareBalance: Double,       // Current shares owned
    val sharePrice: Double = 2000.0,// Fixed ₹2000/share

    // Membership Dates
    val joiningDate: Date,             // Membership start date
    val exitDate: Date? = null,     // Null if active

    // Member Roles
    val role: MemberRole,

    // Status
    val shareholderStatus: String = STATUS_ACTIVE,
    val lastUpdated: Date = Date(),  // Auto-timestamp

    // Audit Fields
    var createdAt: Date = Date(),    // Set on creation
    var updatedAt: Date = Date()     // Updated on changes
) {
    companion object {
        // Status Constants
        const val STATUS_ACTIVE = "Active"
        const val STATUS_INACTIVE = "Inactive"

        // Business Rules
        const val MIN_SHARES = 1
        const val ELIGIBILITY_PERCENTAGE = 0.90 // 90% of share value

        // ID Generation
        fun generateNextId(lastId: String?): String {
            return lastId?.let {
                val num = it.removePrefix("SH").toInt()
                "SH%03d".format(num + 1)
            } ?: "SH001"
        }
    }

    /** Calculates maximum borrowable amount (90% of total share value) */
    fun calculateMaxBorrowAmount(): Double {
        return (shareBalance * sharePrice) * ELIGIBILITY_PERCENTAGE
    }

    fun isEligibleForLoan(): Boolean {
        return shareholderStatus == STATUS_ACTIVE &&
                shareBalance >= MIN_SHARES
    }

    fun isValid(): Boolean {
        return fullName.isNotBlank() &&
                mobileNumber.length == 10 &&
                shareBalance >= MIN_SHARES &&
                role != null
    }

    fun isActive(): Boolean = exitDate == null &&
            shareholderStatus == STATUS_ACTIVE


}

