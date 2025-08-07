package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ShareholderDao {

    /* CREATE */
    @Insert(onConflict = OnConflictStrategy.ABORT) // Or REPLACE, depending on your logic
    suspend fun insert(shareholder: Shareholder)

    /* Alternative insert with REPLACE (optional) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShareholder(shareholder: Shareholder)

    /* READ */
    @Query("SELECT * FROM shareholders ORDER BY fullName ASC")
    fun getAll(): Flow<List<Shareholder>>

    @Query("SELECT * FROM shareholders ORDER BY fullName ASC")
    fun getAllShareholdersFlow(): Flow<List<Shareholder>>

    @Query("SELECT * FROM shareholders WHERE shareholderId = :id")
    suspend fun getById(id: String): Shareholder?

    @Query("SELECT * FROM shareholders WHERE shareholderId = :id")
    suspend fun getShareholderById(id: String): Shareholder?

    @Query("""
        SELECT *, (shareBalance * 2000 * 0.9) AS maxBorrowAmount 
        FROM shareholders 
        WHERE status = 'Active' 
        AND shareBalance >= 1
        AND shareholderId = :id
    """)
    suspend fun getWithBorrowEligibility(id: String): ShareholderWithEligibility?

    @Query("""
        SELECT * FROM shareholders 
        WHERE fullName LIKE :query OR shareholderId LIKE :query
    """)
    suspend fun searchShareholders(query: String): List<Shareholder>

    /* UPDATE */
    @Update
    suspend fun update(shareholder: Shareholder)

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
            status = :newStatus,
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

    /* DELETE */
    @Delete
    suspend fun delete(shareholder: Shareholder)

    @Query("DELETE FROM shareholders WHERE shareholderId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM shareholders WHERE shareholderId = :id")
    suspend fun deleteShareholder(id: String)

    /* UTILITY QUERIES */
    @Query("SELECT shareholderId FROM shareholders ORDER BY shareholderId DESC LIMIT 1")
    suspend fun getLastId(): String?

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM shareholders 
            WHERE shareholderId = :id 
            AND status = 'Active' 
            AND shareBalance >= 1
        )
    """)
    suspend fun isEligibleForLoan(id: String): Boolean

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

}

/* Custom result mappings */
data class ShareholderWithEligibility(
    @Embedded val shareholder: Shareholder,
    val maxBorrowAmount: Double
)

data class ShareholderLoanStatus(
    @Embedded val shareholder: Shareholder,
    val maxBorrowAmount: Double,
    val currentLoans: Double
)
