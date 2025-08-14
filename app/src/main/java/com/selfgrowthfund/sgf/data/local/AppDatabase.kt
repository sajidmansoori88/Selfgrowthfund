package com.selfgrowthfund.sgf.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.selfgrowthfund.sgf.data.local.converters.*
import com.selfgrowthfund.sgf.data.local.converters.common.DateConverters
import com.selfgrowthfund.sgf.data.local.converters.enums.StatusConverters
import com.selfgrowthfund.sgf.data.local.converters.custom.DueMonthConverter
import com.selfgrowthfund.sgf.data.local.dao.*
import com.selfgrowthfund.sgf.data.local.entities.*

// ✅ Compile-time constant
private const val DB_VERSION = 3

@Database(
    entities = [
        Shareholder::class,
        Deposit::class,
        DepositEntry::class,
        Borrowing::class,
        Repayment::class,
        InvestmentReturns::class,
        Investment::class
    ],
    version = DB_VERSION,
    exportSchema = true
)
@TypeConverters(
    DateConverters::class,
    StatusConverters::class,
    DueMonthConverter::class,
    AppTypeConverters::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shareholderDao(): ShareholderDao
    abstract fun depositDao(): DepositDao
    abstract fun depositEntryDao(): DepositEntryDao
    abstract fun borrowingDao(): BorrowingDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun investmentDao(): InvestmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // === MIGRATIONS ===
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE borrowings (
                        borrowId TEXT PRIMARY KEY NOT NULL,
                        shareholderId TEXT NOT NULL,
                        shareholderName TEXT NOT NULL,
                        applicationDate INTEGER NOT NULL,
                        amountRequested REAL NOT NULL,
                        consentingMember1Id TEXT,
                        consentingMember1Name TEXT,
                        consentingMember2Id TEXT,
                        consentingMember2Name TEXT,
                        borrowEligibility REAL NOT NULL,
                        approvedAmount REAL NOT NULL,
                        borrowStartDate INTEGER NOT NULL,
                        dueDate INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'Pending',
                        notes TEXT,
                        createdBy TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(shareholderId) REFERENCES shareholders(id)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE repayments (
                        repaymentId TEXT PRIMARY KEY NOT NULL,
                        borrowId TEXT NOT NULL,
                        amount REAL NOT NULL,
                        paymentDate INTEGER NOT NULL,
                        receivedBy TEXT NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        notes TEXT,
                        FOREIGN KEY(borrowId) REFERENCES borrowings(borrowId)
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX idx_borrowings_shareholder ON borrowings(shareholderId)")
                db.execSQL("CREATE INDEX idx_repayments_borrowing ON repayments(borrowId)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE investments (
                        investmentId TEXT PRIMARY KEY NOT NULL,
                        investeeType TEXT NOT NULL,
                        investeeName TEXT,
                        ownershipType TEXT NOT NULL,
                        partnerNames TEXT,
                        investmentDate INTEGER NOT NULL,
                        investmentType TEXT NOT NULL,
                        investmentName TEXT NOT NULL,
                        amount REAL NOT NULL,
                        expectedProfitPercent REAL NOT NULL,
                        expectedProfitAmount REAL NOT NULL,
                        expectedReturnPeriod INTEGER NOT NULL,
                        returnDueDate INTEGER NOT NULL,
                        modeOfPayment TEXT NOT NULL,
                        status TEXT NOT NULL,
                        remarks TEXT
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX idx_investments_status ON investments(status)")
                db.execSQL("CREATE INDEX idx_investments_dueDate ON investments(returnDueDate)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "self_growth_fund.db"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getTestDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration(false)
                .build()
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL("INSERT INTO shareholders(id, name) VALUES('DEFAULT', 'System Account')")
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }
}
