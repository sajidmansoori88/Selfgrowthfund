package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.data.local.types.BorrowingStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BorrowingDao {

    /* Create Operations */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBorrowing(borrowing: Borrowing)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(borrowings: List<Borrowing>)

    /* Read Operations */
    @Query("SELECT * FROM borrowings ORDER BY applicationDate DESC")
    fun getAllBorrowings(): Flow<List<Borrowing>>

    @Query("SELECT * FROM borrowings WHERE borrowId = :borrowId")
    suspend fun getBorrowingById(borrowId: String): Borrowing?

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId ORDER BY applicationDate DESC")
    fun getBorrowingsByShareholder(shareholderId: String): Flow<List<Borrowing>>

    @Query("SELECT * FROM borrowings WHERE status = :status ORDER BY applicationDate DESC")
    fun getBorrowingsByStatus(status: String): Flow<List<Borrowing>>

    /* Update Operations */
    @Update
    suspend fun updateBorrowing(borrowing: Borrowing)

    @Query("UPDATE borrowings SET status = :newStatus WHERE borrowId = :borrowId")
    suspend fun updateStatus(borrowId: String, newStatus: BorrowingStatus)

    /* Delete Operations */
    @Delete
    suspend fun deleteBorrowing(borrowing: Borrowing)

    @Query("DELETE FROM borrowings WHERE borrowId = :borrowId")
    suspend fun deleteById(borrowId: String)

    /* Special Queries */
    @Query("SELECT COUNT(*) FROM borrowings WHERE shareholderId = :shareholderId AND status NOT IN ('Completed', 'Rejected')")
    suspend fun getActiveLoanCount(shareholderId: String): Int

    @Query("SELECT SUM(approvedAmount) FROM borrowings WHERE shareholderId = :shareholderId AND status = 'Active'")
    suspend fun getTotalActiveLoanAmount(shareholderId: String): Double

    @Query("SELECT * FROM borrowings WHERE dueDate < :currentDate AND status = 'Active'")
    fun getOverdueBorrowings(currentDate: LocalDateTime): Flow<List<Borrowing>>

    /* ID Generation Helper */
    @Query("SELECT borrowId FROM borrowings ORDER BY borrowId DESC LIMIT 1")
    suspend fun getLastBorrowingId(): String?

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId AND status = :status")
    suspend fun getBorrowingsByShareholder(shareholderId: String, status: BorrowingStatus): List<Borrowing>

    @Query("UPDATE borrowings SET status = :status, closedDate = :closedDate WHERE borrowId = :borrowId")
    suspend fun updateBorrowingStatus(
        borrowId: String,
        status: BorrowingStatus,
        closedDate: LocalDateTime?
    )

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId ORDER BY applicationDate DESC")
    fun getBorrowingsByShareholderFlow(shareholderId: String): Flow<List<Borrowing>>

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId AND status = :status ORDER BY applicationDate DESC")
    suspend fun getBorrowingsByShareholderWithStatus(shareholderId: String, status: BorrowingStatus): List<Borrowing>

    @Query("SELECT * FROM borrowings WHERE shareholderId = :shareholderId")
    suspend fun getBorrowingsByShareholderList(shareholderId: String): List<Borrowing>
}