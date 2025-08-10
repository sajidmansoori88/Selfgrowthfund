package com.selfgrowthfund.sgf.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    // Migration from version 1 to 2 (complete schema overhaul)
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. First create new tables with proper constraints
            createShareholdersTable(db)
            createBorrowingsTable(db)
            createRepaymentsTable(db)

            // 2. Then migrate existing data if needed
            migrateExistingShareholders(db)

            // 3. Add performance indexes
            createIndexes(db)
        }

        private fun createShareholdersTable(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS shareholders (
                    shareholderId TEXT PRIMARY KEY NOT NULL,
                    fullName TEXT NOT NULL,
                    mobileNumber TEXT NOT NULL,
                    address TEXT NOT NULL,
                    joinDate INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    exitDate INTEGER,
                    shareBalance REAL NOT NULL DEFAULT 0
                )
            """)
        }

        private fun createBorrowingsTable(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS borrowings (
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
                    FOREIGN KEY(shareholderId) REFERENCES shareholders(shareholderId)
                )
            """)
        }

        private fun createRepaymentsTable(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS repayments (
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
        }

        private fun migrateExistingShareholders(db: SupportSQLiteDatabase) {
            // Example migration if you had an old shareholders table
            db.execSQL("""
                INSERT INTO shareholders (shareholderId, fullName, mobileNumber, address, joinDate, status, shareBalance)
                SELECT 
                    id, 
                    name, 
                    COALESCE(phone, 'Unknown'), 
                    COALESCE(address, 'Unknown'), 
                    strftime('%s', 'now') * 1000, 
                    CASE WHEN active = 1 THEN 'Active' ELSE 'Inactive' END,
                    COALESCE(shares, 0)
                FROM old_shareholders
            """)
        }

        private fun createIndexes(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_shareholders_status ON shareholders(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_borrowings_shareholder ON borrowings(shareholderId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_borrowings_status ON borrowings(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_repayments_borrowing ON repayments(borrowId)")
        }
    }

    // Migration from version 2 to 3 (future changes)
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Example: Add email field to shareholders
            db.execSQL("ALTER TABLE shareholders ADD COLUMN email TEXT")

            // Example: Add loan purpose field to borrowings
            db.execSQL("ALTER TABLE borrowings ADD COLUMN purpose TEXT NOT NULL DEFAULT 'General'")
        }
    }
}