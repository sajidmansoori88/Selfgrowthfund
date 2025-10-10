package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.reports.ShareholderBasicInfo
import com.selfgrowthfund.sgf.model.reports.ShareholderLoanStatus
import com.selfgrowthfund.sgf.model.reports.ShareholderWithEligibility
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface ShareholderDao {

    // ─────────────── CREATE ───────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShareholder(shareholder: Shareholder)

    // ─────────────── READ ───────────────
    @Query("SELECT * FROM shareholders ORDER BY CAST(SUBSTR(shareholderId, 3) AS INTEGER) ASC")
    fun getAllShareholdersFlow(): Flow<List<Shareholder>>

    @Query("SELECT * FROM shareholders WHERE shareholderId = :id")
    suspend fun getShareholderById(id: String): Shareholder?

    @Query("SELECT * FROM shareholders WHERE shareholderId = :id")
    fun getShareholderByIdFlow(id: String): Flow<Shareholder?>

    @Query("SELECT * FROM shareholders ORDER BY shareholderId DESC LIMIT 1")
    suspend fun getLastShareholder(): Shareholder?

    @Query("SELECT shareholderId FROM shareholders ORDER BY shareholderId DESC LIMIT 1")
    suspend fun getLastShareholderId(): String?

    @Query("SELECT shareholderId, fullName, shareBalance, joiningDate FROM shareholders")
    suspend fun getAllShareholders(): List<ShareholderBasicInfo>

    // ❌ FIXED: Removed suspend from Flow-returning method
    @Query("""
    SELECT *, (shareBalance * 2000 * 0.9) AS maxBorrowAmount
    FROM shareholders
    WHERE shareholderStatus = 'Active'
    AND shareBalance >= 1
    AND shareholderId = :id
""")
    fun getWithBorrowEligibilityFlow(id: String): Flow<ShareholderWithEligibility?>

    @Query("""
        SELECT *, (shareBalance * 2000 * 0.9) AS maxBorrowAmount
        FROM shareholders
        WHERE shareholderStatus = 'Active'
        AND shareBalance >= 1
        AND shareholderId = :id
    """)
    suspend fun getWithBorrowEligibility(id: String): ShareholderWithEligibility?

    @Query("""
        SELECT * FROM shareholders
        WHERE fullName LIKE '%' || :query || '%' OR shareholderId LIKE '%' || :query || '%'
        LIMIT 50
    """)
    suspend fun searchShareholders(query: String): List<Shareholder>

    @Query("""
        SELECT
            s.*,
            (s.shareBalance * 2000 * 0.9) AS maxBorrowAmount,
            COALESCE(SUM(b.approvedAmount), 0) AS currentLoans
        FROM shareholders s
        LEFT JOIN borrowings b ON s.shareholderId = b.shareholderId AND b.status != 'Completed'
        WHERE s.shareholderId = :id
        GROUP BY s.shareholderId
    """)
    suspend fun getLoanStatus(id: String): ShareholderLoanStatus?

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM shareholders
            WHERE shareholderId = :id
            AND shareholderStatus = 'Active'
            AND shareBalance >= 1
        )
    """)
    suspend fun isEligibleForLoan(id: String): Boolean

    @Query("SELECT * FROM shareholders WHERE role = :role")
    suspend fun getByRole(role: MemberRole): List<Shareholder>

    // ─────────────── UPDATE ───────────────
    @Update
    suspend fun updateShareholder(shareholder: Shareholder)

    @Query("""
        UPDATE shareholders
        SET
            shareBalance = shareBalance + :amount,
            lastUpdated = :timestamp
        WHERE shareholderId = :id
    """)
    suspend fun updateShareBalance(id: String, amount: Double, timestamp: Instant)

    @Query("""
        UPDATE shareholders
        SET
            shareholderStatus = :newStatus,
            exitDate = CASE WHEN :newStatus = 'Inactive' THEN :exitDate ELSE NULL END,
            lastUpdated = :timestamp
        WHERE shareholderId = :id
    """)
    suspend fun updateStatus(
        id: String,
        newStatus: String,
        exitDate: LocalDate?,
        timestamp: Instant
    )

    // ─────────────── DELETE ───────────────
    @Delete
    suspend fun deleteShareholder(shareholder: Shareholder)

    @Query("DELETE FROM shareholders WHERE shareholderId = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM shareholders")
    suspend fun getAll(): List<Shareholder>

    @Update
    suspend fun update(shareholder: Shareholder)
}
