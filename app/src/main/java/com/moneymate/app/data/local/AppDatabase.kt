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
        BookAdjustment::class
    ],
    version = 12,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun personDao(): PersonDao
    abstract fun paymentDao(): PaymentDao
    abstract fun editRequestDao(): EditRequestDao
    abstract fun defaultPersonDao(): DefaultPersonDao
    abstract fun bookAdjustmentDao(): BookAdjustmentDao

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
    }
}
