package com.moneymate.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.local.entity.*

@Database(
    entities = [
        LoanFile::class,
        Person::class,
        Payment::class,
        EditRequest::class,
        DefaultPerson::class,
        BookAdjustment::class,
        Expense::class,
        Investment::class,
        ExpenseCategory::class,
        InvestmentType::class,
        Area::class,
        AppUser::class,
        AuditLog::class
    ],
    version = 16,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun personDao(): PersonDao
    abstract fun paymentDao(): PaymentDao
    abstract fun editRequestDao(): EditRequestDao
    abstract fun defaultPersonDao(): DefaultPersonDao
    abstract fun bookAdjustmentDao(): BookAdjustmentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun investmentTypeDao(): InvestmentTypeDao
    abstract fun areaDao(): AreaDao
    abstract fun appUserDao(): AppUserDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE persons ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE persons ADD COLUMN mobileNumber TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE persons ADD COLUMN recordType TEXT NOT NULL DEFAULT 'LENDING'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS default_persons (
                        id TEXT NOT NULL PRIMARY KEY,
                        nlrKey TEXT NOT NULL,
                        name TEXT NOT NULL,
                        place TEXT,
                        mobileNumber TEXT,
                        amountGiven REAL NOT NULL DEFAULT 0.0,
                        mode TEXT NOT NULL DEFAULT 'CASH',
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        recordType TEXT NOT NULL DEFAULT 'LENDING',
                        isSeeded INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE persons ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE persons ADD COLUMN completedAt INTEGER")
                database.execSQL("ALTER TABLE persons ADD COLUMN linkedNewPersonId TEXT")
                database.execSQL("ALTER TABLE persons ADD COLUMN isPendingNewLoan INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE persons ADD COLUMN previousPersonId TEXT")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DELETE FROM default_persons")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE loan_files ADD COLUMN defaultInterestRate REAL NOT NULL DEFAULT 25.0")
                database.execSQL("ALTER TABLE loan_files ADD COLUMN defaultCalculationMode TEXT NOT NULL DEFAULT 'FLAT'")
                database.execSQL("ALTER TABLE persons ADD COLUMN interestRate REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE persons ADD COLUMN interestAmount REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE persons ADD COLUMN totalRepayment REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE persons ADD COLUMN loanType TEXT NOT NULL DEFAULT 'MONTHLY'")
                database.execSQL("ALTER TABLE persons ADD COLUMN numberOfInstallments INTEGER NOT NULL DEFAULT 10")
                database.execSQL("ALTER TABLE persons ADD COLUMN perInstallmentAmount REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE persons ADD COLUMN isDurationBased INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE persons ADD COLUMN durationDays INTEGER")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE persons ADD COLUMN interestType TEXT NOT NULL DEFAULT 'PERCENTAGE'")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                if (!columnExists(database, "loan_files", "isDeleted")) database.execSQL("ALTER TABLE loan_files ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                if (!columnExists(database, "loan_files", "deletedAt")) database.execSQL("ALTER TABLE loan_files ADD COLUMN deletedAt INTEGER")
                if (!columnExists(database, "persons", "isDeleted")) database.execSQL("ALTER TABLE persons ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                if (!columnExists(database, "persons", "deletedAt")) database.execSQL("ALTER TABLE persons ADD COLUMN deletedAt INTEGER")
                if (!columnExists(database, "payments", "isDeleted")) database.execSQL("ALTER TABLE payments ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                if (!columnExists(database, "payments", "deletedAt")) database.execSQL("ALTER TABLE payments ADD COLUMN deletedAt INTEGER")
            }

            private fun columnExists(database: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
                val cursor = database.query("PRAGMA table_info($tableName)", emptyArray())
                cursor.use {
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        if (cursor.getString(nameIndex) == columnName) return true
                    }
                }
                return false
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS book_adjustments (
                        id TEXT NOT NULL PRIMARY KEY,
                        personId TEXT NOT NULL,
                        fileId TEXT NOT NULL,
                        discrepancyAmount REAL NOT NULL,
                        type TEXT NOT NULL,
                        reason TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        approvedByAdmin INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                if (!columnExists(database, "persons", "photoUri")) database.execSQL("ALTER TABLE persons ADD COLUMN photoUri TEXT")
                if (!columnExists(database, "persons", "alternateMobile")) database.execSQL("ALTER TABLE persons ADD COLUMN alternateMobile TEXT")
                if (!columnExists(database, "persons", "address")) database.execSQL("ALTER TABLE persons ADD COLUMN address TEXT")
                if (!columnExists(database, "persons", "businessType")) database.execSQL("ALTER TABLE persons ADD COLUMN businessType TEXT")
                if (!columnExists(database, "persons", "maxLoanAmount")) database.execSQL("ALTER TABLE persons ADD COLUMN maxLoanAmount REAL")
                if (!columnExists(database, "persons", "guarantorPersonId")) database.execSQL("ALTER TABLE persons ADD COLUMN guarantorPersonId TEXT")
                if (!columnExists(database, "persons", "customerCode")) database.execSQL("ALTER TABLE persons ADD COLUMN customerCode TEXT")
                if (!columnExists(database, "persons", "subCode")) database.execSQL("ALTER TABLE persons ADD COLUMN subCode TEXT")
                if (!columnExists(database, "persons", "badLoanDays")) database.execSQL("ALTER TABLE persons ADD COLUMN badLoanDays INTEGER NOT NULL DEFAULT 90")
                if (!columnExists(database, "persons", "sendSms")) database.execSQL("ALTER TABLE persons ADD COLUMN sendSms INTEGER NOT NULL DEFAULT 0")
            }

            private fun columnExists(database: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
                val cursor = database.query("PRAGMA table_info($tableName)", emptyArray())
                cursor.use {
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        if (cursor.getString(nameIndex) == columnName) return true
                    }
                }
                return false
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        fileId TEXT NOT NULL,
                        category TEXT NOT NULL,
                        amount REAL NOT NULL,
                        isOnline INTEGER NOT NULL DEFAULT 0,
                        date INTEGER NOT NULL,
                        notes TEXT,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY (fileId) REFERENCES loan_files(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_fileId ON expenses(fileId)")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS investments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        fileId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        isOnline INTEGER NOT NULL DEFAULT 0,
                        date INTEGER NOT NULL,
                        notes TEXT,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY (fileId) REFERENCES loan_files(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_investments_fileId ON investments(fileId)")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS expense_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0
                    )
                """)
                database.execSQL("INSERT OR IGNORE INTO expense_categories(name, isDefault) VALUES ('Rent', 1)")
                database.execSQL("INSERT OR IGNORE INTO expense_categories(name, isDefault) VALUES ('Fuel', 1)")
                database.execSQL("INSERT OR IGNORE INTO expense_categories(name, isDefault) VALUES ('Salary', 1)")
                database.execSQL("INSERT OR IGNORE INTO expense_categories(name, isDefault) VALUES ('Maintenance', 1)")
                database.execSQL("INSERT OR IGNORE INTO expense_categories(name, isDefault) VALUES ('Miscellaneous', 1)")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS investment_types (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0
                    )
                """)
                database.execSQL("INSERT OR IGNORE INTO investment_types(name, isDefault) VALUES ('Capital', 1)")
                database.execSQL("INSERT OR IGNORE INTO investment_types(name, isDefault) VALUES ('Equipment', 1)")
                database.execSQL("INSERT OR IGNORE INTO investment_types(name, isDefault) VALUES ('Other', 1)")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS areas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        fileId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_areas_fileId ON areas(fileId)")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS app_users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        email TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        role TEXT NOT NULL,
                        assignedFileIds TEXT NOT NULL DEFAULT '',
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        createdByUserId INTEGER NOT NULL,
                        pinHash TEXT,
                        lastLoginAt INTEGER,
                        isDeleted INTEGER NOT NULL DEFAULT 0
                    )
                """)
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_app_users_email ON app_users(email)")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS audit_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        userId INTEGER NOT NULL,
                        userEmail TEXT NOT NULL,
                        action TEXT NOT NULL,
                        targetType TEXT NOT NULL,
                        targetId TEXT NOT NULL,
                        targetLabel TEXT NOT NULL,
                        details TEXT,
                        timestamp INTEGER NOT NULL,
                        fileId TEXT
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_audit_logs_userId ON audit_logs(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_audit_logs_fileId ON audit_logs(fileId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_audit_logs_target ON audit_logs(targetType, targetId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp)")
            }
        }
    }
}
