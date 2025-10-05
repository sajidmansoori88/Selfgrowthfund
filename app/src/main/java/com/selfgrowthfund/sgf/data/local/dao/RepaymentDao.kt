package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RepaymentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(repayment: Repayment)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(repayments: List<Repayment>)

    @Update
    suspend fun update(repayment: Repayment)

    @Delete
    suspend fun delete(repayment: Repayment)

    @Query("DELETE FROM repayments WHERE provisional_id = :provisionalId")
    suspend fun deleteByProvisionalId(provisionalId: String)

    @Query("SELECT * FROM repayments ORDER BY repaymentDate DESC")
    fun getAllRepayments(): Flow<List<Repayment>>

    @Query("SELECT * FROM repayments WHERE provisional_id = :provisionalId LIMIT 1")
    suspend fun getByProvisionalId(provisionalId: String): Repayment?

    @Query("SELECT * FROM repayments WHERE borrowId = :borrowId ORDER BY repaymentDate DESC")
    fun getByBorrowId(borrowId: String): Flow<List<Repayment>>

    @Query("SELECT * FROM repayments WHERE borrowId = :borrowId ORDER BY repaymentDate DESC")
    suspend fun getByBorrowIdList(borrowId: String): List<Repayment>

    @Query("SELECT * FROM repayments WHERE borrowId = :borrowId ORDER BY repaymentDate DESC LIMIT 1")
    suspend fun getLastRepayment(borrowId: String): Repayment?

    @Query("UPDATE repayments SET borrowingStatus = :status WHERE provisional_id = :provisionalId")
    suspend fun updateBorrowingStatus(provisionalId: String, status: String)

    @Query("SELECT repayment_id FROM repayments ORDER BY repaymentDate DESC LIMIT 1")
    suspend fun getLastRepaymentId(): String?

    // 🔑 NEW: only fetch approved repayments with final IDs
    @Query("SELECT repayment_id FROM repayments WHERE approval_status = 'ADMIN_APPROVED' ORDER BY repaymentDate DESC LIMIT 1")
    suspend fun getLastApprovedRepaymentId(): String?

    @Query("SELECT SUM(principalRepaid) FROM repayments WHERE borrowId = :borrowId")
    suspend fun getTotalPrincipalRepaid(borrowId: String): Double

    @Query("SELECT SUM(penaltyPaid) FROM repayments WHERE borrowId = :borrowId")
    suspend fun getTotalPenaltyPaid(borrowId: String): Double

    @Query("""
        SELECT COUNT(*) as count,
               SUM(principalRepaid) as totalPrincipal,
               SUM(penaltyPaid) as totalPenalty
        FROM repayments
        WHERE borrowId = :borrowId
    """)
    suspend fun getBorrowingRepaymentSummary(borrowId: String): BorrowingRepaymentSummary

    @Query("""
        SELECT r.*
        FROM repayments r
        JOIN borrowings b ON r.borrowId = b.borrowId
        WHERE r.repaymentDate > date(b.dueDate, '+45 days')
        ORDER BY r.repaymentDate DESC
    """)
    suspend fun getLateRepayments(): List<Repayment>

    @Query("""
        SELECT * FROM repayments
        WHERE notes LIKE '%' || :query || '%'
        OR repayment_id LIKE '%' || :query || '%'
        ORDER BY repaymentDate DESC
        LIMIT 50
    """)
    suspend fun searchRepayments(query: String): List<Repayment>

    // 🔄 Approval workflow
    @Query("""
        UPDATE repayments
        SET approval_status = :status,
            approved_by = :approvedBy,
            approval_notes = :notes,
            updated_at = :updatedAt
        WHERE provisional_id = :provisionalId
    """)
    suspend fun updateApprovalStatus(
        provisionalId: String,
        status: ApprovalStage,
        approvedBy: String?,
        notes: String?,
        updatedAt: Long
    ): Int

    // 🔑 NEW: approve and assign final repaymentId
    @Query("""
        UPDATE repayments
        SET repayment_id = :newId,
            approval_status = :status,
            approved_by = :approvedBy,
            approval_notes = :notes,
            updated_at = :updatedAt
        WHERE provisional_id = :provisionalId
    """)
    suspend fun approveRepayment(
        provisionalId: String,
        newId: String,
        status: ApprovalStage,
        approvedBy: String?,
        notes: String?,
        updatedAt: LocalDate
    ): Int

    @Query("""
        SELECT COUNT(*) FROM repayments
        WHERE approval_status = :status
          AND date(createdAt) BETWEEN :start AND :end
    """)
    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Int

    @Query("SELECT COUNT(*) FROM repayments WHERE date(createdAt) BETWEEN :start AND :end")
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int

    @Query("""
        SELECT * FROM repayments
        WHERE date(updated_at) BETWEEN :start AND :end
    """)
    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Repayment>

    @Query("SELECT * FROM repayments WHERE provisional_id = :provisionalId LIMIT 1")
    suspend fun findById(provisionalId: String): Repayment?

    @Query("SELECT * FROM repayments WHERE approval_status = :status")
    suspend fun getByApprovalStatus(status: ApprovalStage): List<Repayment>

    // --- Summaries ---
    data class BorrowingRepaymentSummary(
        @ColumnInfo(name = "count") val count: Int,
        @ColumnInfo(name = "totalPrincipal") val totalPrincipal: Double?,
        @ColumnInfo(name = "totalPenalty") val totalPenalty: Double?
    )

    @Query("SELECT SUM(principalRepaid) FROM repayments")
    suspend fun getTotalPrincipalRepaidAll(): Double

    @Query("SELECT SUM(principalRepaid + penaltyPaid) FROM repayments")
    suspend fun getTotalRepaidAll(): Double

    @Query("""
        SELECT strftime('%Y-%m', repaymentDate) AS month,
               SUM(principalRepaid + penaltyPaid) AS total
        FROM repayments
        GROUP BY month
        ORDER BY month ASC
    """)
    suspend fun getMonthlyRepayments(): List<MonthlyAmount>

    @Query("""
        SELECT SUM(principalRepaid + penaltyPaid)
        FROM repayments
        WHERE strftime('%Y-%m', repaymentDate) = :month
    """)
    suspend fun getMonthlyRepayments(month: String): Double

    @Query("""
    SELECT provisional_id AS provisionalId,
           repayment_id AS repaymentId,
           borrowId,
           shareholderName,
           repaymentDate,
           principalRepaid,
           penaltyPaid,
           (principalRepaid + penaltyPaid) AS totalAmountPaid,
           modeOfPayment,
           finalOutstanding,
           approval_status AS approvalStatus,
           createdAt
    FROM repayments
    ORDER BY repaymentDate DESC
""")
    fun getRepaymentSummaries(): Flow<List<com.selfgrowthfund.sgf.data.local.dto.RepaymentSummaryDTO>>

}

