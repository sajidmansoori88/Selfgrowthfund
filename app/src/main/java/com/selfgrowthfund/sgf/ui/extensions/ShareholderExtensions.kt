package com.selfgrowthfund.sgf.ui.extensions

import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.utils.IdGenerator
import java.time.Instant

// ✅ Friendly label shortcut
val Shareholder.roleLabel: String
    get() = this.role.label // Enum has a label property

val ShareholderEntry.roleLabel: String
    get() = this.role.label

// ✅ Convert ShareholderEntry to Shareholder (safe for enum-based role)
fun ShareholderEntry.toShareholder(lastId: String?): Shareholder {
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
        role = role, // ✅ Already a MemberRole
        createdAt = Instant.now()
    )
}

// ✅ String → Enum helpers for Firestore or legacy data
fun String.toMemberRoleOrDefault(): MemberRole =
    try {
        MemberRole.valueOf(this)
    } catch (_: IllegalArgumentException) {
        // If you ever store custom labels instead of .name in Firestore:
        MemberRole.entries.firstOrNull { it.label.equals(this, ignoreCase = true) }
            ?: MemberRole.MEMBER
    }

// ✅ Firestore mapping for backward compatibility
fun Map<String, Any>.toMemberRole(): MemberRole =
    (this["role"] as? String)?.toMemberRoleOrDefault() ?: MemberRole.MEMBER