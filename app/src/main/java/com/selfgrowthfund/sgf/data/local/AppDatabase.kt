package com.selfgrowthfund.sgf.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.selfgrowthfund.sgf.data.local.converters.Converters
import com.selfgrowthfund.sgf.data.local.dao.*
import com.selfgrowthfund.sgf.data.local.entities.*
import java.util.*

@Database(
    entities = [
        Shareholder::class,
        Deposit::class,
        Borrowing::class,
        Repayment::class,
        Investment::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shareholderDao(): ShareholderDao
    abstract fun depositDao(): DepositDao
    abstract fun borrowingDao(): BorrowingDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun investmentDao(): InvestmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Define all migrations
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
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
                """)

                database.execSQL("""
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
                """)

                database.execSQL("CREATE INDEX idx_borrowings_shareholder ON borrowings(shareholderId)")
                database.execSQL("CREATE INDEX idx_repayments_borrowing ON repayments(borrowId)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
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
                """)

                database.execSQL("CREATE INDEX idx_investments_status ON investments(status)")
                database.execSQL("CREATE INDEX idx_investments_dueDate ON investments(returnDueDate)")
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
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Initialize with default data
            db.execSQL("INSERT INTO shareholders(id, name) VALUES('DEFAULT', 'System Account')")
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }
}