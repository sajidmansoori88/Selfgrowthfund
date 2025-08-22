package com.selfgrowthfund.sgf.ui.extensions

import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.enums.ShareholderStatus
import java.time.Instant
import java.time.LocalDate

fun ShareholderEntry.toShareholder(lastId: String?): Shareholder {
    val newId = Shareholder.generateNextId(lastId)
    return Shareholder(
        shareholderId = newId,
        fullName = this.fullName,
        dob = this.dob ?: LocalDate.now(),
        email = this.email,
        mobileNumber = this.mobileNumber,
        address = this.address,
        shareBalance = this.shareBalance,
        sharePrice = 2000.0,
        joiningDate = this.joiningDate ?: LocalDate.now(),
        exitDate = null,
        role = MemberRole.valueOf(  this.role),
        shareholderStatus = ShareholderStatus.Active,
        lastUpdated = Instant.now(),
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )
}

// Reverse conversion
fun Shareholder.toShareholderEntry(): ShareholderEntry {
    return ShareholderEntry(
        fullName = this.fullName,
        mobileNumber = this.mobileNumber,
        email = this.email,
        dob = this.dob,
        address = this.address,
        shareBalance = this.shareBalance,
        joiningDate = this.joiningDate,
        role = this.role.name
    )
}