package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao.*
import com.selfgrowthfund.sgf.data.local.types.PaymentMode
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RepaymentRepository @Inject constructor(
    private val dao: RepaymentDao
) {
    // Helper for Flow conversions
    private fun <T> Flow<T>.asResult(): Flow<Result<T>> =
        this
            .map<T, Result<T>> { value -> Result.Success(value) }
            .catch { e -> emit(Result.Error<T>(e)) }

    // CRUD Operations
    suspend fun insert(repayment: Repayment): Result<Unit> =
        try { dao.insert(repayment); Result.Success(Unit) }
        catch (e: Exception) { Result.Error(e) }

    suspend fun update(repayment: Repayment): Result<Unit> =
        try { dao.update(repayment); Result.Success(Unit) }
        catch (e: Exception) { Result.Error(e) }

    suspend fun delete(repayment: Repayment): Result<Unit> =
        try { dao.delete(repayment); Result.Success(Unit) }
        catch (e: Exception) { Result.Error(e) }

    suspend fun getById(repaymentId: String): Result<Repayment?> =
        try { Result.Success(dao.getById(repaymentId)) }
        catch (e: Exception) { Result.Error(e) }

    // Borrowing-specific
    suspend fun getAllByBorrowIdList(borrowId: String): Result<List<Repayment>> =
        try { Result.Success(dao.getByBorrowIdList(borrowId)) }
        catch (e: Exception) { Result.Error(e) }

    fun getAllByBorrowId(borrowId: String): Flow<Result<List<Repayment>>> =
        dao.getByBorrowIdFlow(borrowId).asResult()

    suspend fun getLastRepayment(borrowId: String): Result<Repayment?> =
        try { Result.Success(dao.getLastRepayment(borrowId)) }
        catch (e: Exception) { Result.Error(e) }

    // Aggregates
    suspend fun getTotalPrincipalRepaid(borrowId: String): Result<Double> =
        try { Result.Success(dao.getTotalPrincipalRepaid(borrowId)) }
        catch (e: Exception) { Result.Error(e) }

    suspend fun getTotalPenaltyPaid(borrowId: String): Result<Double> =
        try { Result.Success(dao.getTotalPenaltyPaid(borrowId)) }
        catch (e: Exception) { Result.Error(e) }

    suspend fun getBorrowingRepaymentSummary(borrowId: String): Result<BorrowingRepaymentSummary> =
        try { Result.Success(dao.getBorrowingRepaymentSummary(borrowId)) }
        catch (e: Exception) { Result.Error(e) }

    // Payment Mode
    suspend fun countByPaymentMode(borrowId: String, mode: PaymentMode): Result<Int> =
        try { Result.Success(dao.countByPaymentMode(borrowId, mode)) }
        catch (e: Exception) { Result.Error(e) }

    suspend fun getMonthlyTotalByPaymentMode(mode: PaymentMode, monthYear: String): Result<Double?> =
        try { Result.Success(dao.getMonthlyTotalByPaymentMode(mode, monthYear)) }
        catch (e: Exception) { Result.Error(e) }

    // Reports
    suspend fun getMonthlyShareholderReport(shareholderName: String): Result<List<MonthlyRepaymentReport>> =
        try { Result.Success(dao.getMonthlyShareholderReport(shareholderName)) }
        catch (e: Exception) { Result.Error(e) }

    // Late payments
    suspend fun getLateRepayments(shareholderName: String): Result<List<Repayment>> =
        try { Result.Success(dao.getLateRepayments(shareholderName)) }
        catch (e: Exception) { Result.Error(e) }

    // Search
    suspend fun searchRepayments(query: String): Result<List<Repayment>> =
        try { Result.Success(dao.searchRepayments(query)) }
        catch (e: Exception) { Result.Error(e) }

    // Detailed info
    suspend fun getRepaymentsWithBorrowingDetails(borrowId: String): Result<List<RepaymentWithBorrowingDetails>> =
        try { Result.Success(dao.getRepaymentsWithBorrowingDetails(borrowId)) }
        catch (e: Exception) { Result.Error(e) }
}