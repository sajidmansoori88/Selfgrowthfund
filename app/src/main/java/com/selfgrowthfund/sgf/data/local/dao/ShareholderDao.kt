package com.selfgrowthfund.sgf.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.local.types.ShareholderStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ShareholderDao {

    // ─────────────── CREATE OPERATIONS ───────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShareholder(shareholder: Shareholder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shareholders: List<Shareholder>)

    // ─────────────── READ OPERATIONS ───────────────
    @Query("SELECT * FROM shareholders ORDER BY fullName ASC")
    fun getAllShareholdersFlow(): Flow<List<Shareholder>>

    @Query("SELECT * FROM shareholders ORDER BY fullName ASC")
    fun getPagedShareholders(): PagingSource<Int, Shareholder>

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
        WHERE status = :status
        AND shareBalance >= :minShares
        AND shareholderId = :id
    """)
    suspend fun getWithBorrowEligibility(
        id: String,
        status: ShareholderStatus = ShareholderStatus.Active,
        minShares: Int = 1
    ): ShareholderWithEligibility?

    @Query("""
        SELECT * FROM shareholders
        WHERE fullName LIKE '%' || :query || '%' 
        OR shareholderId LIKE '%' || :query || '%'
        LIMIT 50
    """)
    suspend fun searchShareholders(query: String): List<Shareholder>

    @Query("""
        SELECT
            s.*,
            (s.shareBalance * 2000 * 0.9) AS maxBorrowAmount,
            COALESCE(SUM(b.approvedAmount), 0) AS currentLoans
        FROM shareholders s
        LEFT JOIN borrowings b ON s.shareholderId = b.shareholderId 
        AND b.status != 'COMPLETED'
        WHERE s.shareholderId = :id
        GROUP BY s.shareholderId
    """)
    suspend fun getLoanStatus(id: String): ShareholderLoanStatus?

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM shareholders
            WHERE shareholderId = :id
            AND status = :status
            AND shareBalance >= :minShares
        )
    """)
    suspend fun isEligibleForLoan(
        id: String,
        status: ShareholderStatus = ShareholderStatus.Active,
        minShares: Int = 1
    ): Boolean

    // ─────────────── UPDATE OPERATIONS ───────────────
    @Update
    suspend fun updateShareholder(shareholder: Shareholder)

    @Query("""
        UPDATE shareholders
        SET shareBalance = shareBalance + :amount,
            lastUpdated = :timestamp
        WHERE shareholderId = :id
    """)
    suspend fun updateShareBalance(
        id: String,
        amount: Double,
        timestamp: LocalDateTime
    )

    @Query("""
        UPDATE shareholders
        SET status = :status,
            exitDate = CASE WHEN :status = 'INACTIVE' THEN :exitDate ELSE NULL END,
            lastUpdated = :timestamp
        WHERE shareholderId = :id
    """)
    suspend fun updateStatus(
        id: String,
        status: ShareholderStatus,
        exitDate: LocalDateTime?,
        timestamp: LocalDateTime
    )

    @Query("UPDATE shareholders SET status = 'DELETED' WHERE shareholderId = :id")
    suspend fun softDelete(id: String)

    // ─────────────── DELETE OPERATIONS ───────────────
    @Delete
    suspend fun deleteShareholder(shareholder: Shareholder)

    @Query("DELETE FROM shareholders WHERE shareholderId = :id")
    suspend fun deleteById(id: String)
}

// ─────────────── RESULT CLASSES ───────────────
data class ShareholderWithEligibility(
    @Embedded val shareholder: Shareholder,
    val maxBorrowAmount: Double
) {
    val remainingEligibility: Double get() = maxBorrowAmount
}

data class ShareholderLoanStatus(
    @Embedded val shareholder: Shareholder,
    val maxBorrowAmount: Double,
    val currentLoans: Double
) {
    val availableCredit: Double get() = maxBorrowAmount - currentLoans
}