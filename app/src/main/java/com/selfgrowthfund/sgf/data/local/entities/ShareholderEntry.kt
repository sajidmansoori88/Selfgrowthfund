package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.enums.ShareholderStatus
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.utils.IdGenerator
import java.time.Instant
import java.time.LocalDate

@TypeConverters(AppTypeConverters::class)
@Entity(tableName = "shareholder_entries")
data class ShareholderEntry(
    @PrimaryKey(autoGenerate = true)
    val entryId: Long = 0L,              // Auto ID for staging records
    val shareholderId: String = "",

    val fullName: String,
    val mobileNumber: String,
    val email: String,
    val dob: LocalDate? = null,          // Optional
    val address: String,

    val shareBalance: Double = 0.0,
    val joiningDate: LocalDate? = null,  // Optional until approved

    // ✅ Now enum-based, consistent with Shareholder
    val role: MemberRole = MemberRole.MEMBER,
    val status: ShareholderStatus = ShareholderStatus.Active
) {

    fun toShareholder(lastId: String?): Shareholder {
        val newId = IdGenerator.nextShareholderId(lastId)

        return Shareholder(
            shareholderId = newId,
            fullName = fullName,
            mobileNumber = mobileNumber,
            email = email,
            dob = dob,
            address = address,
            shareBalance = shareBalance,
            joiningDate = joiningDate,
            role = role, // ✅ Directly assign enum
            createdAt = Instant.now()
        )
    }
}