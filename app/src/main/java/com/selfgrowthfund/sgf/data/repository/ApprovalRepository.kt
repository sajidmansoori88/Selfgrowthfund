package com.selfgrowthfund.sgf.data.repository


import com.selfgrowthfund.sgf.model.ApprovalEntry
import com.selfgrowthfund.sgf.model.ApprovalGroup
import com.selfgrowthfund.sgf.model.ApprovalHistoryEntry
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.CurrentUser
import com.selfgrowthfund.sgf.ui.admin.ApprovalSummaryRow
import java.time.LocalDate
import javax.inject.Inject

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}

class ApprovalRepository @Inject constructor(
    private val depositRepository: DepositRepository,
    private val borrowingRepository: BorrowingRepository,
    private val repaymentRepository: RepaymentRepository,
    private val investmentRepository: InvestmentRepository,
    private val investmentReturnsRepository: InvestmentReturnsRepository,
    private val otherIncomeRepository: OtherIncomeRepository,
    private val otherExpenseRepository: OtherExpenseRepository,
    private val approvalFlowRepository: ApprovalFlowRepository,
    private val sessionRepository: UserSessionRepository
) {

    // --- Summary Dashboard ---
    suspend fun getApprovalSummary(start: LocalDate, end: LocalDate): List<ApprovalSummaryRow> {
        return listOf(
            ApprovalSummaryRow("Deposits",
                approved = depositRepository.countApproved(start, end),
                rejected = depositRepository.countRejected(start, end),
                pending = depositRepository.countPending(start, end)
            ),
            ApprovalSummaryRow("Borrowing",
                approved = borrowingRepository.countApproved(start, end),
                rejected = borrowingRepository.countRejected(start, end),
                pending = borrowingRepository.countPending(start, end)
            ),
            ApprovalSummaryRow("Repayments",
                approved = repaymentRepository.countApproved(start, end),
                rejected = repaymentRepository.countRejected(start, end),
                pending = repaymentRepository.countPending(start, end)
            ),
            ApprovalSummaryRow("Investments",
                approved = investmentRepository.countApproved(start, end),
                rejected = investmentRepository.countRejected(start, end),
                pending = investmentRepository.countPending(start, end)
            ),
            ApprovalSummaryRow("Investment Returns",
                approved = investmentReturnsRepository.countApproved(start, end),
                rejected = investmentReturnsRepository.countRejected(start, end),
                pending = investmentReturnsRepository.countPending(start, end)
            ),
            ApprovalSummaryRow("Other Incomes",
                approved = otherIncomeRepository.countApproved(start, end),
                rejected = otherIncomeRepository.countRejected(start, end),
                pending = otherIncomeRepository.countPending(start, end)
            ),
            ApprovalSummaryRow("Other Expenses",
                approved = otherExpenseRepository.countApproved(start, end),
                rejected = otherExpenseRepository.countRejected(start, end),
                pending = otherExpenseRepository.countPending(start, end)
            )
        )
    }

    suspend fun getGroupedApprovals(): List<ApprovalGroup> {
        // TODO: aggregate from all repositories when you design group logic
        return emptyList()
    }

    // --- Approvals (Wrappers) ---
    suspend fun approveEntry(entry: ApprovalEntry, currentUser: CurrentUser): Result<Unit> {
        return approveEntryInternal(
            approvalType = entry.type,
            provisionalId = entry.entityId,
            approverRole = currentUser.role,
            approverId = currentUser.userId,
            notes = entry.notes
        )
    }

    suspend fun rejectEntry(entry: ApprovalEntry, currentUser: CurrentUser): Result<Unit> {
        return rejectEntryInternal(
            approvalType = entry.type,
            provisionalId = entry.entityId,
            rejectedBy = currentUser.userId,
            reason = entry.notes
        )
    }

    // --- Internal implementations ---
    private suspend fun approveEntryInternal(
        approvalType: ApprovalType,
        provisionalId: String,
        approverRole: MemberRole,
        approverId: String,
        notes: String? = null
    ): Result<Unit> {
        return try {
            when (approvalType) {
                ApprovalType.DEPOSIT -> {
                    when (approverRole) {
                        MemberRole.MEMBER_TREASURER ->
                            depositRepository.approveByTreasurer(provisionalId, approverId, notes)
                        MemberRole.MEMBER_ADMIN ->
                            depositRepository.approveByAdmin(provisionalId, approverId, null, notes)
                        else -> throw IllegalArgumentException("Invalid role for deposit approval")
                    }
                }

                ApprovalType.BORROWING -> borrowingRepository.approve(provisionalId, approverId, notes)

                ApprovalType.REPAYMENT -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    repaymentRepository.approve(provisionalId, approverId, notes, newStatus)
                }

                ApprovalType.INVESTMENT -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    investmentRepository.approve(provisionalId, approverId, notes, newStatus)
                }

                ApprovalType.INVESTMENT_RETURN -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    investmentReturnsRepository.approve(provisionalId, approverId, notes, newStatus)
                }

                ApprovalType.OTHER_INCOME -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    otherIncomeRepository.approve(provisionalId.toLong(), approverId, notes, newStatus)
                }

                ApprovalType.OTHER_EXPENSE -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    otherExpenseRepository.approve(provisionalId.toLong(), approverId, notes, newStatus)
                }

                ApprovalType.ALL -> {
                    throw IllegalArgumentException("ApprovalType.ALL is not valid for approveEntry()")
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun rejectEntryInternal(
        approvalType: ApprovalType,
        provisionalId: String,
        rejectedBy: String,
        reason: String? = null
    ): Result<Unit> {
        return try {
            when (approvalType) {
                ApprovalType.DEPOSIT -> depositRepository.reject(provisionalId, rejectedBy, reason)
                ApprovalType.BORROWING -> borrowingRepository.reject(provisionalId, rejectedBy, reason)
                ApprovalType.REPAYMENT -> repaymentRepository.reject(provisionalId, rejectedBy, reason)
                ApprovalType.INVESTMENT -> investmentRepository.reject(provisionalId, rejectedBy, reason)
                ApprovalType.INVESTMENT_RETURN -> investmentReturnsRepository.reject(provisionalId, rejectedBy, reason)
                ApprovalType.OTHER_INCOME -> otherIncomeRepository.reject(provisionalId.toLong(), rejectedBy, reason)
                ApprovalType.OTHER_EXPENSE -> otherExpenseRepository.reject(provisionalId.toLong(), rejectedBy, reason)
                ApprovalType.ALL -> throw IllegalArgumentException("ApprovalType.ALL is not valid for rejectEntry()")
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // --- History ---
    suspend fun getApprovalHistory(
        start: LocalDate,
        end: LocalDate,
        type: ApprovalType
    ): List<ApprovalHistoryEntry> {
        val allHistory = aggregateApprovalHistoryFromAllRepos(start, end)
        return if (type == ApprovalType.ALL) allHistory else allHistory.filter { it.type == type }
    }

    private suspend fun aggregateApprovalHistoryFromAllRepos(
        start: LocalDate,
        end: LocalDate
    ): List<ApprovalHistoryEntry> {
        val startInstant = start.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
        val endInstant = end.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)

        val flows = approvalFlowRepository.getAllFlowsBetween(startInstant, endInstant)

        val history = flows.map { flow ->
            val shareholderId: String? = when (flow.entityType) {
                ApprovalType.DEPOSIT -> depositRepository.findById(flow.entityId)?.shareholderId
                ApprovalType.BORROWING -> borrowingRepository.findById(flow.entityId)?.shareholderId
                ApprovalType.REPAYMENT -> {
                    val repayment = repaymentRepository.findById(flow.entityId)
                    repayment?.borrowId?.let { borrowId ->
                        borrowingRepository.getBorrowingById(borrowId).shareholderId
                    }
                }
                ApprovalType.INVESTMENT -> investmentRepository.findById(flow.entityId)?.shareholderId
                ApprovalType.INVESTMENT_RETURN -> {
                    val investmentReturn = investmentReturnsRepository.findById(flow.entityId)
                    investmentReturn?.investmentId?.let { invId ->
                        investmentRepository.getByInvestmentId(invId)?.shareholderId
                    }
                }
                ApprovalType.OTHER_INCOME -> null
                ApprovalType.OTHER_EXPENSE -> null
                else -> null
            }

            ApprovalHistoryEntry(
                id = flow.entityId,
                type = flow.entityType,
                approvedBy = flow.approvedBy,
                date = flow.approvedAt.toString(),
                status = flow.action,
                shareholderId = shareholderId
            )
        }

        return history.sortedBy { it.date }
    }
}
