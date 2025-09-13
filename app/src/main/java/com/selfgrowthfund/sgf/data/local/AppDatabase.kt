package com.selfgrowthfund.sgf.data.local

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.data.local.dao.*
import com.selfgrowthfund.sgf.data.local.entities.*

@Database(
    entities = [
        Shareholder::class,
        Deposit::class,
        DepositEntry::class,
        Borrowing::class,
        Repayment::class,
        Investment::class,
        InvestmentReturns::class,
        ActionItem::class,
        Penalty::class,
        Income::class,
        Expense::class
    ],
    version = AppDatabase.VERSION,
    exportSchema = true
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val VERSION = 4

    }

    abstract fun shareholderDao(): ShareholderDao
    abstract fun depositDao(): DepositDao
    abstract fun depositEntryDao(): DepositEntryDao
    abstract fun borrowingDao(): BorrowingDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun investmentReturnsDao(): InvestmentReturnsDao
    abstract fun actionItemDao(): ActionItemDao
    abstract fun penaltyDao(): PenaltyDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao

    internal class DatabaseCallback : Callback() {
        // Add any database initialization code here if needed
    }
}