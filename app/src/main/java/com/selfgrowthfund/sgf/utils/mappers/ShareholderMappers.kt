package com.selfgrowthfund.sgf.utils.mappers

import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import java.time.LocalDate

// ShareholderEntry → Shareholder
fun ShareholderEntry.toShareholder(lastId: String?): Shareholder {
    return Shareholder(
        shareholderId = Shareholder.generateNextId(lastId),
        fullName = fullName,
        dob = dob ?: LocalDate.now(),
        email = email,
        mobileNumber = mobileNumber,
        address = address,
        shareBalance = shareBalance,
        joiningDate = joiningDate ?: LocalDate.now(),
        role = role // ✅ Already a MemberRole
    )
}

// Shareholder → ShareholderEntry
fun Shareholder.toEntry(): ShareholderEntry {
    return ShareholderEntry(
        fullName = fullName,
        mobileNumber = mobileNumber,
        email = email,
        dob = dob,
        address = address,
        shareBalance = shareBalance,
        joiningDate = joiningDate,
        role = role // ✅ Already a MemberRole
    )
}