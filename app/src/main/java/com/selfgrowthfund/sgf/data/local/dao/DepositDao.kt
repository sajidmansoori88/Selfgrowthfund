package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.reports.MonthlyAmount
import com.selfgrowthfund.sgf.model.reports.ShareholderDepositSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DepositDao {

    // --- Basic Queries ---
    @Query("SELECT * FROM deposits ORDER BY paymentDate DESC")
    fun getAll(): Flow<List<Deposit>>

    @Query("SELECT * FROM deposits WHERE shareholderId = :id")
    fun getByShareholder(id: String): Flow<List<Deposit>>

    @Query("SELECT depositId FROM deposits ORDER BY depositId DESC LIMIT 1")
    suspend fun getLastId(): String?

    @Query("SELECT SUM(totalAmount) FROM deposits WHERE strftime('%Y-%m', createdAt) = :monthYear")
    suspend fun getMonthlyTotal(monthYear: String): Double

    @Query("SELECT SUM(shareNos) FROM deposits")
    suspend fun getTotalShareCount(): Int

    @Query("SELECT * FROM deposits ORDER BY dueMonth DESC")
    fun getAllDeposits(): List<Deposit>

    @Query("SELECT SUM(additionalContribution) FROM deposits")
    suspend fun getAdditionalContributions(): Double

    @Query("""
        SELECT strftime('%Y-%m', paymentDate) AS month, SUM(totalAmount) AS total
        FROM deposits
        GROUP BY month
        ORDER BY month ASC
    """)
    suspend fun getMonthlyDeposits(): List<MonthlyAmount>

    @Query("""
        SELECT shareholderId, SUM(totalAmount) AS totalDeposits, MAX(paymentDate) AS lastDate
        FROM deposits
        GROUP BY shareholderId
    """)
    suspend fun getShareholderDepositSummary(): List<ShareholderDepositSummary>

    @Query("SELECT SUM(totalAmount) FROM deposits")
    suspend fun getTotalFundDeposit(): Double

    @Query("SELECT SUM(shareAmount * shareNos) FROM deposits")
    suspend fun getTotalShareAmount(): Double

    @Query("""
        SELECT DISTINCT strftime('%Y-%m', paymentDate)
        FROM deposits
        ORDER BY paymentDate ASC
    """)
    suspend fun getActiveMonths(): List<String>

    @Query("""
        SELECT SUM(totalAmount)
        FROM deposits
        WHERE strftime('%Y-%m', paymentDate) = :month
    """)
    suspend fun getMonthlyIncome(month: String): Double

    @Query("""
        SELECT SUM(shareAmount * shareNos)
        FROM deposits
        WHERE strftime('%Y-%m', paymentDate) = :month
    """)
    suspend fun getMonthlyShareDeposit(month: String): Double

    @Query("SELECT * FROM deposits WHERE shareholderId = :id ORDER BY paymentDate LIMIT 1")
    suspend fun getLastDepositForShareholder(id: String): Deposit?

    // --- Approvals ---
    @Query("""
        UPDATE deposits 
        SET approvalStatus = :status, 
            depositId = :depositId, 
            approvedBy = :approvedBy, 
            notes = :notes,
            updatedAt = :timestamp 
        WHERE provisionalId = :provisionalId
    """)
    suspend fun approveByAdmin(
        provisionalId: String,
        depositId: String,
        status: ApprovalStage,
        approvedBy: String,
        notes: String?,
        timestamp: Long
    )

    @Query("""
        UPDATE deposits 
        SET approvalStatus = :status, 
            approvedBy = :approvedBy, 
            notes = :notes,
            updatedAt = :timestamp 
        WHERE provisionalId = :provisionalId
    """)
    suspend fun updateStatus(
        provisionalId: String,
        status: ApprovalStage,
        approvedBy: String,
        notes: String?,
        timestamp: Long
    )

    // --- Counts & Reports ---
    @Query("""
        SELECT COUNT(*) FROM deposits 
        WHERE approvalStatus = :status 
        AND paymentDate BETWEEN :start AND :end
    """)
    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Int

    @Query("""
        SELECT COUNT(*) FROM deposits 
        WHERE paymentDate BETWEEN :start AND :end
    """)
    suspend fun countTotal(start: LocalDate, end: LocalDate): Int

    @Query("""
        SELECT * FROM deposits 
        WHERE paymentDate BETWEEN :start AND :end
    """)
    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Deposit>

    // --- Finders ---
    @Query("SELECT * FROM deposits WHERE depositId = :depositId LIMIT 1")
    suspend fun findById(depositId: String): Deposit?

    @Query("SELECT * FROM deposits WHERE provisionalId = :id LIMIT 1")
    suspend fun getByProvisionalId(id: String): Deposit?

    @Update
    suspend fun update(deposit: Deposit)

    @Query("SELECT * FROM deposits WHERE depositId = :id LIMIT 1")
    suspend fun getByDepositId(id: String): Deposit?

    @Query("SELECT * FROM deposits WHERE approvalStatus = :stage")
    suspend fun getByApprovalStage(stage: ApprovalStage): List<Deposit>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deposit: Deposit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(deposits: List<Deposit>)

    // --- Summaries ---
    @Query("""
        SELECT 
            provisionalId, 
            depositId, 
            shareholderId, 
            shareholderName, 
            shareNos, 
            shareAmount, 
            additionalContribution, 
            penalty, 
            totalAmount, 
            paymentStatus, 
            dueMonth, 
            paymentDate, 
            createdAt,
            modeOfPayment
        FROM deposits
        ORDER BY createdAt DESC
    """)
    fun getDepositEntrySummaries(): Flow<List<DepositEntrySummaryDTO>>
}
