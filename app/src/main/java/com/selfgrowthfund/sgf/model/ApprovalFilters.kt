package com.selfgrowthfund.sgf.model

import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.model.enums.ExportType
import com.selfgrowthfund.sgf.model.enums.MemberRole

object ApprovalFilters {

    fun allowedApprovalTypes(role: MemberRole): List<ApprovalType> = when (role) {
        MemberRole.MEMBER_ADMIN -> ApprovalType.entries // ALL types
        MemberRole.MEMBER_TREASURER -> listOf(
            ApprovalType.DEPOSIT,
            ApprovalType.BORROWING,
            ApprovalType.REPAYMENT,
            ApprovalType.INVESTMENT,
            ApprovalType.INVESTMENT_RETURN
        )
        MemberRole.MEMBER -> listOf(
            ApprovalType.INVESTMENT,
            ApprovalType.BORROWING
        )
    }

    fun allowedExportTypes(role: MemberRole): List<ExportType> = when (role) {
        MemberRole.MEMBER_ADMIN,
        MemberRole.MEMBER_TREASURER,
        MemberRole.MEMBER -> ExportType.getAll() // CSV + PDF
    }
}
