package com.selfgrowthfund.sgf.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            createShareholdersTable(db)
            createBorrowingsTable(db)
            createRepaymentsTable(db)
            migrateExistingShareholders(db)
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
            try {
                db.execSQL("""
                    INSERT INTO shareholders (shareholderId, fullName, mobileNumber, address, joinDate, status, shareBalance)
                    SELECT id, name, COALESCE(phone, 'Unknown'), COALESCE(address, 'Unknown'),
                           strftime('%s','now')*1000,
                           CASE WHEN active=1 THEN 'Active' ELSE 'Inactive' END,
                           COALESCE(shares,0)
                    FROM old_shareholders
                """)
            } catch (e: Exception) {
                // old_shareholders not present — ignore
            }
        }

        private fun createIndexes(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_shareholders_status ON shareholders(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_borrowings_shareholder ON borrowings(shareholderId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_borrowings_status ON borrowings(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_repayments_borrowing ON repayments(borrowId)")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shareholders ADD COLUMN email TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE borrowings ADD COLUMN purpose TEXT NOT NULL DEFAULT 'General'")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE deposit_entries ADD COLUMN status TEXT NOT NULL DEFAULT 'Pending'")
            db.execSQL("ALTER TABLE deposit_entries ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE deposit_entries ADD COLUMN entrySource TEXT NOT NULL DEFAULT 'USER'")
        }
    }
}
