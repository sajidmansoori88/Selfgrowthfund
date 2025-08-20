package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ShareholderDao {

    // ─────────────── CREATE ───────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShareholder(shareholder: Shareholder)

    // ─────────────── READ ───────────────
    @Query("SELECT * FROM shareholders ORDER BY fullName ASC")
    fun getAllShareholdersFlow(): Flow<List<Shareholder>>

    @Query("SELECT * FROM shareholders WHERE shareholderId = :id")
    suspend fun getShareholderById(id: String): Shareholder?

    @Query("SELECT * FROM shareholders WHERE shareholderId = :id")
    fun getShareholderByIdFlow(id: String): Flow<Shareholder?>

    @Query("SELECT * FROM shareholders ORDER BY shareholderId DESC LIMIT 1")
    suspend fun getLastShareholder(): Shareholder?

    @Query("SELECT shareholderId FROM shareholders ORDER BY shareholderId DESC LIMIT 1")
    suspend fun getLastId(): String?

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
        WHERE fullName LIKE :query OR shareholderId LIKE :query
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
    suspend fun updateShareBalance(id: String, amount: Double, timestamp: Date)

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
        exitDate: Date?,
        timestamp: Date
    )

    // ─────────────── DELETE ───────────────
    @Delete
    suspend fun deleteShareholder(shareholder: Shareholder)

    @Query("DELETE FROM shareholders WHERE shareholderId = :id")
    suspend fun deleteById(id: String)
}

// ─────────────── Custom Result Mappings ───────────────

data class ShareholderWithEligibility(
    @Embedded val shareholder: Shareholder,
    val maxBorrowAmount: Double
)

data class ShareholderLoanStatus(
    @Embedded val shareholder: Shareholder,
    val maxBorrowAmount: Double,
    val currentLoans: Double
)
