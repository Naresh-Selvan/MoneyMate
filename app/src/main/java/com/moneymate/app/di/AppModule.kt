package com.moneymate.app.di

import android.content.Context
import androidx.room.Room
import com.moneymate.app.data.local.AppDatabase
import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.repository.DefaultPersonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "moneymate_db"
        ).addMigrations(
            object : androidx.room.migration.Migration(2, 3) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE persons ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                }
            },
            object : androidx.room.migration.Migration(3, 4) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE persons ADD COLUMN mobileNumber TEXT")
                }
            },
            object : androidx.room.migration.Migration(4, 5) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE persons ADD COLUMN recordType TEXT NOT NULL DEFAULT 'LENDING'")
                }
            },
            object : androidx.room.migration.Migration(5, 6) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
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
            },
            // v7: completion / rollover columns on persons
            object : androidx.room.migration.Migration(6, 7) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE persons ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE persons ADD COLUMN completedAt INTEGER")
                    database.execSQL("ALTER TABLE persons ADD COLUMN linkedNewPersonId TEXT")
                    database.execSQL("ALTER TABLE persons ADD COLUMN isPendingNewLoan INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE persons ADD COLUMN previousPersonId TEXT")
                }
            }
        ).build()
    }

    @Provides fun provideFileDao(db: AppDatabase): FileDao = db.fileDao()
    @Provides fun providePersonDao(db: AppDatabase): PersonDao = db.personDao()
    @Provides fun providePaymentDao(db: AppDatabase): PaymentDao = db.paymentDao()
    @Provides fun provideEditRequestDao(db: AppDatabase): EditRequestDao = db.editRequestDao()
    @Provides fun provideDefaultPersonDao(db: AppDatabase): DefaultPersonDao = db.defaultPersonDao()

    @Provides
    @Singleton
    fun provideDefaultPersonRepository(dao: DefaultPersonDao): DefaultPersonRepository =
        DefaultPersonRepository(dao)
}
