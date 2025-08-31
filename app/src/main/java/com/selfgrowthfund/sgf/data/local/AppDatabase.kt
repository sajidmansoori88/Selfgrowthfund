package com.selfgrowthfund.sgf.data.local

import android.content.Context
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

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "self_growth_fund.db"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(
                        Migrations.MIGRATION_1_2,
                        Migrations.MIGRATION_2_3,
                        Migrations.MIGRATION_3_4
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
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

    internal class DatabaseCallback : RoomDatabase.Callback() {
        // Add any database initialization code here if needed
    }
}