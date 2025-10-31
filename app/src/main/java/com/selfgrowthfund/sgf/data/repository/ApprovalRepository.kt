package com.selfgrowthfund.sgf.data.repository


import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.model.ApprovalEntry
import com.selfgrowthfund.sgf.model.ApprovalGroup
import com.selfgrowthfund.sgf.model.ApprovalHistoryEntry
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
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

        // Generic unwrap that works for both Int and Result<Int>
        fun unwrapCount(result: Any?): Int = when (result) {
            is com.selfgrowthfund.sgf.utils.Result<*> -> when (result) {
                is com.selfgrowthfund.sgf.utils.Result.Success<*> -> result.data as? Int ?: 0
                is com.selfgrowthfund.sgf.utils.Result.Error -> 0
                else -> 0  // 🔸 covers “when expression must be exhaustive”
            }
            is Int -> result
            else -> 0
        }


        return listOf(
            ApprovalSummaryRow(
                "Deposits",
                approved = unwrapCount(depositRepository.countApproved(start, end)),
                rejected = unwrapCount(depositRepository.countRejected(start, end)),
                pending = unwrapCount(depositRepository.countPending(start, end))
            ),
            ApprovalSummaryRow(
                "Borrowing",
                approved = unwrapCount(borrowingRepository.countApproved(start, end)),
                rejected = unwrapCount(borrowingRepository.countRejected(start, end)),
                pending = unwrapCount(borrowingRepository.countPending(start, end))
            ),
            ApprovalSummaryRow(
                "Repayments",
                approved = unwrapCount(repaymentRepository.countApproved(start, end)),
                rejected = unwrapCount(repaymentRepository.countRejected(start, end)),
                pending = unwrapCount(repaymentRepository.countPending(start, end))
            ),
            ApprovalSummaryRow(
                "Investments",
                approved = unwrapCount(investmentRepository.countApproved(start, end)),
                rejected = unwrapCount(investmentRepository.countRejected(start, end)),
                pending = unwrapCount(investmentRepository.countPending(start, end))
            ),
            ApprovalSummaryRow(
                "Investment Returns",
                approved = unwrapCount(investmentReturnsRepository.countApproved(start, end)),
                rejected = unwrapCount(investmentReturnsRepository.countRejected(start, end)),
                pending = unwrapCount(investmentReturnsRepository.countPending(start, end))
            ),
            ApprovalSummaryRow(
                "Other Incomes",
                approved = unwrapCount(otherIncomeRepository.countApproved(start, end)),
                rejected = unwrapCount(otherIncomeRepository.countRejected(start, end)),
                pending = unwrapCount(otherIncomeRepository.countPending(start, end))
            ),
            ApprovalSummaryRow(
                "Other Expenses",
                approved = unwrapCount(otherExpenseRepository.countApproved(start, end)),
                rejected = unwrapCount(otherExpenseRepository.countRejected(start, end)),
                pending = unwrapCount(otherExpenseRepository.countPending(start, end))
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
                            depositRepository.approveByTreasurer(provisionalId, approverId, notes ?: "")
                        MemberRole.MEMBER_ADMIN ->
                            depositRepository.approveByAdmin(provisionalId, approverId, null, notes ?: "")
                        else -> throw IllegalArgumentException("Invalid role for deposit approval")
                    }
                }

                ApprovalType.BORROWING ->
                    borrowingRepository.approve(provisionalId, approverId, notes ?: "")

                ApprovalType.REPAYMENT -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    repaymentRepository.approve(provisionalId, approverId, notes ?: "", newStatus)
                }

                ApprovalType.INVESTMENT -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    investmentRepository.approve(provisionalId, approverId, notes ?: "", newStatus)
                }

                ApprovalType.INVESTMENT_RETURN -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    investmentReturnsRepository.approve(provisionalId, approverId, notes ?: "", newStatus)
                }

                ApprovalType.OTHER_INCOME -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    otherIncomeRepository.approve(provisionalId.toLong(), approverId, notes ?: "", newStatus)
                }

                ApprovalType.OTHER_EXPENSE -> {
                    val newStatus = when (approverRole) {
                        MemberRole.MEMBER_TREASURER -> ApprovalStage.TREASURER_APPROVED
                        MemberRole.MEMBER_ADMIN -> ApprovalStage.ADMIN_APPROVED
                        else -> ApprovalStage.APPROVED
                    }
                    otherExpenseRepository.approve(provisionalId.toLong(), approverId, notes ?: "", newStatus)
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

    // --- Member Voting Logic (2/3 Rule) ---
    suspend fun recordMemberVote(
        approvalType: ApprovalType,
        entityId: String,
        memberId: String,
        isApproved: Boolean,
        remarks: String? = null
    ): Result<Unit> {
        return try {
            // 1️⃣ Record this member’s vote
            val flow = ApprovalFlow(
                entityType = approvalType,
                entityId = entityId,
                role = MemberRole.MEMBER,
                action = if (isApproved)
                    ApprovalAction.APPROVE
                else
                    ApprovalAction.REJECT,
                approvedBy = memberId,
                remarks = remarks
            )
            approvalFlowRepository.recordApproval(flow)

            // 2️⃣ Count votes
            val approvedCount =
                approvalFlowRepository.countApprovedByEntity(entityId, approvalType)
            val totalMembers = sessionRepository.getTotalActiveMembers()  // We'll use DAO soon

            // 3️⃣ Calculate threshold (2/3 of active members)
            val required = kotlin.math.ceil(totalMembers * (2.0 / 3.0)).toInt()

            // 4️⃣ Check if 2/3 approved
            if (approvedCount >= required) {
                when (approvalType) {
                    ApprovalType.INVESTMENT -> {
                        investmentRepository.approve(
                            provisionalId = entityId,
                            approverId = memberId,
                            notes = "2/3 members approved",
                            newStatus = ApprovalStage.APPROVED
                        )
                    }
                    ApprovalType.BORROWING -> {
                        borrowingRepository.approve(
                            provisionalId = entityId,
                            approverId = memberId,
                            notes = "2/3 members approved"
                        )
                    }
                    else -> {
                        // Optional: Handle other entity types later
                    }
                }
                timber.log.Timber.i("2/3 majority reached for $approvalType ($entityId). Auto-updated to MEMBER_APPROVED.")
            } else {
                timber.log.Timber.i("Vote recorded for $approvalType ($entityId): $approvedCount/$totalMembers approved so far.")
            }

            Result.Success(Unit)

        } catch (e: Exception) {
            timber.log.Timber.e(e, "Error recording member vote for $approvalType ($entityId)")
            Result.Error(e)
        }
    }

    // --- Treasurer & Admin Finalization ---

    // Treasurer marks payment released
    suspend fun markTreasurerRelease(
        entityId: String,
        currentUser: CurrentUser,
        remarks: String? = null
    ): Result<Unit> {
        return try {
            val role = currentUser.role
            if (role != MemberRole.MEMBER_TREASURER) {
                throw IllegalAccessException("Only Treasurer can release payment.")
            }

            when (val result = investmentRepository.markPaymentReleased(
                entityId,
                currentUser.userId,
                remarks
            )) {
                is com.selfgrowthfund.sgf.utils.Result.Success -> {
                    approvalFlowRepository.recordApproval(
                        ApprovalFlow(
                            entityType = ApprovalType.INVESTMENT,
                            entityId = entityId,
                            role = MemberRole.MEMBER_TREASURER,
                            action = ApprovalAction.APPROVE,
                            approvedBy = currentUser.userId,
                            remarks = "Payment Released"
                        )
                    )
                    timber.log.Timber.i("Treasurer marked payment released for $entityId")
                    Result.Success(Unit)
                }
                is com.selfgrowthfund.sgf.utils.Result.Error -> {
                    Result.Error(Exception("Failed to mark Treasurer approval: ${result.exception.message}"))
                }
                com.selfgrowthfund.sgf.utils.Result.Loading -> {
                    Result.Error(Exception("Payment release still in progress. Try again."))
                }
            }
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Treasurer release failed for $entityId")
            Result.Error(e)
        }
    }


    // Admin finalizes the investment (assigns ID + date)
    suspend fun finalizeAdminApproval(
        entityId: String,
        currentUser: CurrentUser,
        remarks: String? = null
    ): Result<Unit> {
        return try {
            if (currentUser.role != MemberRole.MEMBER_ADMIN) {
                throw IllegalAccessException("Only Admin can finalize investment.")
            }

            when (val result = investmentRepository.approveAndAssignId(
                provisionalId = entityId,
                approverId = currentUser.userId,
                notes = remarks
            )) {
                is com.selfgrowthfund.sgf.utils.Result.Success -> {
                    approvalFlowRepository.recordApproval(
                        ApprovalFlow(
                            entityType = ApprovalType.INVESTMENT,
                            entityId = entityId,
                            role = MemberRole.MEMBER_ADMIN,
                            action = ApprovalAction.APPROVE,
                            approvedBy = currentUser.userId,
                            remarks = "Final Admin Approval"
                        )
                    )
                    timber.log.Timber.i("Admin finalized investment: $entityId → ID assigned.")
                    Result.Success(Unit)
                }
                is com.selfgrowthfund.sgf.utils.Result.Error -> {
                    Result.Error(Exception("Failed to finalize investment: ${result.exception.message}"))
                }
                com.selfgrowthfund.sgf.utils.Result.Loading -> {
                    Result.Error(Exception("Finalization still in progress. Try again."))
                }
            }
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Admin finalization failed for $entityId")
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
                ApprovalType.BORROWING -> runCatching {
                    borrowingRepository.getBorrowingById(flow.entityId).shareholderId
                }.getOrNull()

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
