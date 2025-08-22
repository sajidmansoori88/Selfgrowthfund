package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.enums.ShareholderStatus
import java.time.LocalDate

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

    val role: String = MemberRole.MEMBER.name, // Fixed: Use .name to get the string value
    val status: ShareholderStatus = ShareholderStatus.Active
)