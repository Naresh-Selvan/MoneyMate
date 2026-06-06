package com.moneymate.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.database.Cursor
import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.local.entity.*

@Database(
    entities = [
        LoanFile::class,
        Person::class,
        Payment::class,
        EditRequest::class,
        DefaultPerson::class   // Table kept in schema; all rows purged by migration 7→8
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun personDao(): PersonDao
    abstract fun paymentDao(): PaymentDao
    abstract fun editRequestDao(): EditRequestDao
    abstract fun defaultPersonDao(): DefaultPersonDao

    companion object {
        /**
         * Migration 8 → 9
         * Adds interest-related columns to loan_files and persons tables.
         *
         * loan_files:
         *   defaultInterestRate  REAL NOT NULL DEFAULT 25.0
         *   defaultCalculationMode TEXT NOT NULL DEFAULT 'FLAT'
         *
         * persons:
         *   interestRate         REAL NOT NULL DEFAULT 0.0
         *   interestAmount       REAL NOT NULL DEFAULT 0.0
         *   totalRepayment       REAL NOT NULL DEFAULT 0.0
         *   loanType             TEXT NOT NULL DEFAULT 'MONTHLY'
         *   numberOfInstallments INTEGER NOT NULL DEFAULT 10
         *   perInstallmentAmount REAL NOT NULL DEFAULT 0.0
         *   isDurationBased      INTEGER NOT NULL DEFAULT 0  (Room stores Boolean as INTEGER)
         *   durationDays         INTEGER (nullable)
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // ── loan_files additions ────────────────────────────────────────
                database.execSQL(
                    "ALTER TABLE loan_files ADD COLUMN defaultInterestRate REAL NOT NULL DEFAULT 25.0"
                )
                database.execSQL(
                    "ALTER TABLE loan_files ADD COLUMN defaultCalculationMode TEXT NOT NULL DEFAULT 'FLAT'"
                )

                // ── persons additions ───────────────────────────────────────────
                database.execSQL(
                    "ALTER TABLE persons ADD COLUMN interestRate REAL NOT NULL DEFAULT 0.0"
                )
                database.execSQL(
                    "ALTER TABLE persons ADD COLUMN interestAmount REAL NOT NULL DEFAULT 0.0"
                )
                database.execSQL(
                    "ALTER TABLE persons ADD COLUMN totalRepayment REAL NOT NULL DEFAULT 0.0"
                )
                database.execSQL(
                    "ALTER TABLE persons ADD COLUMN loanType TEXT NOT NULL DEFAULT 'MONTHLY'"
                )
                database.execSQL(
                    "ALTER TABLE persons ADD COLUMN numberOfInstallments INTEGER NOT NULL DEFAULT 10"
                )
                database.execSQL(
                    "ALTER TABLE persons ADD COLUMN perInstallmentAmount REAL NOT NULL DEFAULT 0.0"
                )
                database.execSQL(
                    "ALTER TABLE persons ADD COLUMN isDurationBased INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE persons ADD COLUMN durationDays INTEGER"
                )
            }
        }

        /**
         * Migration 9 → 10
         * Adds interestType column to persons table.
         *
         * persons:
         *   interestType TEXT NOT NULL DEFAULT 'PERCENTAGE'
         */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE persons ADD COLUMN interestType TEXT NOT NULL DEFAULT 'PERCENTAGE'"
                )
            }
        }

        /**
         * Migration 10 → 11
         * Ensures isDeleted and deletedAt exist on loan_files, persons, and payments.
         */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // loan_files
                if (!columnExists(database, "loan_files", "isDeleted")) {
                    database.execSQL("ALTER TABLE loan_files ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                }
                if (!columnExists(database, "loan_files", "deletedAt")) {
                    database.execSQL("ALTER TABLE loan_files ADD COLUMN deletedAt INTEGER")
                }

                // persons
                if (!columnExists(database, "persons", "isDeleted")) {
                    database.execSQL("ALTER TABLE persons ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                }
                if (!columnExists(database, "persons", "deletedAt")) {
                    database.execSQL("ALTER TABLE persons ADD COLUMN deletedAt INTEGER")
                }

                // payments
                if (!columnExists(database, "payments", "isDeleted")) {
                    database.execSQL("ALTER TABLE payments ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                }
                if (!columnExists(database, "payments", "deletedAt")) {
                    database.execSQL("ALTER TABLE payments ADD COLUMN deletedAt INTEGER")
                }
            }

            private fun columnExists(database: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
                val cursor = database.query("PRAGMA table_info($tableName)", emptyArray())
                cursor.use {
                    val nameIndex = cursor.getColumnIndex("name")
                    if (nameIndex != -1) {
                        while (cursor.moveToNext()) {
                            if (cursor.getString(nameIndex) == columnName) {
                                return true
                            }
                        }
                    }
                }
                return false
            }
        }
    }
}