package com.selfgrowthfund.sgf.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.data.local.converters.EnumConverters
import com.selfgrowthfund.sgf.data.local.dao.*
import com.selfgrowthfund.sgf.data.local.entities.*

@Database(
    entities = [
        Shareholder::class,
        ShareholderEntry::class,
        Deposit::class,
        DepositEntry::class,
        Borrowing::class,
        Repayment::class,
        Investment::class,
        InvestmentReturns::class
    ],
    version = AppDatabase.VERSION,
    exportSchema = true
)
@TypeConverters(AppTypeConverters::class, EnumConverters::class)
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
    abstract fun shareholderEntryDao(): ShareholderEntryDao
    abstract fun depositDao(): DepositDao
    abstract fun depositEntryDao(): DepositEntryDao
    abstract fun borrowingDao(): BorrowingDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun investmentReturnsDao(): InvestmentReturnsDao

    internal class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
               }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
        }
    }
}