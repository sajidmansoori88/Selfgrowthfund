package com.selfgrowthfund.sgf.data.local

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.data.local.dao.*
import com.selfgrowthfund.sgf.data.local.entities.*

@Database(
    entities = [
        Shareholder::class,
        Deposit::class,
        Borrowing::class,
        Repayment::class,
        Investment::class,
        InvestmentReturns::class,
        ActionItem::class,
        Penalty::class,
        OtherIncome::class,
        OtherExpense::class,
        ApprovalFlow::class,
        UserSessionHistory ::class

    ],
    version = AppDatabase.VERSION,
    exportSchema = true
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val VERSION = 5

    }

    abstract fun shareholderDao(): ShareholderDao
    abstract fun depositDao(): DepositDao
    abstract fun borrowingDao(): BorrowingDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun investmentReturnsDao(): InvestmentReturnsDao
    abstract fun actionItemDao(): ActionItemDao
    abstract fun penaltyDao(): PenaltyDao
    abstract fun incomeDao(): OtherIncomeDao
    abstract fun expenseDao(): OtherExpenseDao
    abstract fun approvalFlowDao(): ApprovalFlowDao
    abstract fun userSessionDao(): UserSessionDao

    internal class DatabaseCallback : Callback() {
        // Add any database initialization code here if needed
    }
}